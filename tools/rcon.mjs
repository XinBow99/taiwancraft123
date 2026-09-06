// 對 server 下指令，不需要抓著 server 的 stdin。
//
// 原本的作法是 `tail -f run/console.in | ./gradlew runServer`——用一條管線餵 stdin。
// 問題是那條管線必須有一個行程一直活著，而那個行程被系統以記憶體不足砍掉過四次。
// 每砍一次，server 的 JVM 還活著、玩家還在線上，但我就再也下不了任何指令，
// 只能把 server 整個重開。
//
// RCON 沒有這個問題：它是一條 TCP 連線，要用的時候才開，用完就關。
// server 活著就連得上，中間死掉幾個背景工作都不影響。
//
//   node tools/rcon.mjs "list"
//   node tools/rcon.mjs "give Player967 taiwan:lanbao" "say 車來了"
//
// 密碼與埠號從 run/server.properties 讀。
import { readFileSync } from 'node:fs';
import { connect } from 'node:net';

const props = Object.fromEntries(
  readFileSync('run/server.properties', 'utf8')
    .split(/\r?\n/)
    .filter((l) => l && !l.startsWith('#'))
    .map((l) => [l.slice(0, l.indexOf('=')), l.slice(l.indexOf('=') + 1)]),
);
const PORT = Number(props['rcon.port'] || 25575);
const PASSWORD = props['rcon.password'];
if (props['enable-rcon'] !== 'true' || !PASSWORD) {
  console.error('run/server.properties 裡 enable-rcon 要是 true 且 rcon.password 不能空');
  process.exit(1);
}

const commands = process.argv.slice(2);
if (!commands.length) {
  console.error('用法：node tools/rcon.mjs "<指令>" ["<指令>" ...]');
  process.exit(1);
}

const AUTH = 3;
const EXEC = 2;

// 封包：長度(4) 編號(4) 型別(4) 內容(NUL) 結尾(NUL)。長度不含自己那四個位元組
const packet = (id, type, body) => {
  const payload = Buffer.from(body, 'ascii');
  const buf = Buffer.alloc(14 + payload.length);
  buf.writeInt32LE(10 + payload.length, 0);
  buf.writeInt32LE(id, 4);
  buf.writeInt32LE(type, 8);
  payload.copy(buf, 12);
  return buf;
};

const socket = connect({ host: '127.0.0.1', port: PORT });
socket.setTimeout(10000);

let pending = Buffer.alloc(0);
let next = 0;
const waiting = new Map();

socket.on('data', (chunk) => {
  pending = Buffer.concat([pending, chunk]);
  // 一次可能收到好幾個封包，也可能收到半個
  while (pending.length >= 4) {
    const len = pending.readInt32LE(0);
    if (pending.length < len + 4) break;
    const id = pending.readInt32LE(4);
    const body = pending.subarray(12, len + 2).toString('utf8');
    pending = pending.subarray(len + 4);
    const resolve = waiting.get(id);
    if (resolve) { waiting.delete(id); resolve({ id, body }); }
  }
});

const send = (type, body) => new Promise((resolve, reject) => {
  const id = ++next;
  waiting.set(id, resolve);
  socket.write(packet(id, type, body), (err) => err && reject(err));
});

socket.on('timeout', () => { console.error('RCON 逾時'); process.exit(1); });
socket.on('error', (err) => { console.error('RCON 連不上：' + err.message); process.exit(1); });

socket.on('connect', async () => {
  // 認證失敗的話 server 回的編號是 -1，而那個編號對不到任何一個等待中的請求，
  // 所以這裡用逾時判斷比對編號更可靠
  const auth = await Promise.race([
    send(AUTH, PASSWORD),
    new Promise((r) => setTimeout(() => r(null), 3000)),
  ]);
  if (!auth) { console.error('RCON 密碼錯誤'); process.exit(1); }

  for (const cmd of commands) {
    const { body } = await send(EXEC, cmd);
    const text = body.trim();
    console.log(`> ${cmd}`);
    if (text) console.log(text);
  }
  socket.end();
});
