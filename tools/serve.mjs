import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
const types = { html:'text/html; charset=utf-8', png:'image/png', js:'text/javascript' };
createServer(async (req, res) => {
  const name = decodeURIComponent(req.url.split('?')[0]).replace(/^\/+/, '') || 'model-viewer.html';
  try {
    const body = await readFile(new URL(name, import.meta.url));
    res.writeHead(200, { 'content-type': types[name.split('.').pop()] || 'application/octet-stream' });
    res.end(body);
  } catch { res.writeHead(404).end('not found'); }
}).listen(8731, () => console.log('viewer on http://localhost:8731/'));
