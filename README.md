# taiwan

把 Minecraft 的世界換成台灣的樣子。Fabric 模組，Minecraft 26.2。

一條中央山脈往西降成丘陵、丘陵降成沖積平原、河從山上一路切到海；聚落長在平原與河階上，
街上有騎樓、鐵皮加蓋、電線桿、鐵捲門、會發光的招牌、宮廟與夜市。

## 現況

| 階段 | 內容 | 狀態 |
|---|---|---|
| Phase 1 | 世界生成（地形 + 聚落建築） | 完成 |
| Phase 5 | 台灣獼猴 | 完成（應要求插隊） |
| Phase 2 | 載具（汽車、機車） | 未開始 |
| Phase 3 | 陣頭少年 NPC（群膽機制） | 未開始 |
| Phase 4 | NPC 對話系統（static / LLM） | 未開始 |

## 玩

建立世界時「世界類型」選 **台灣**。專用伺服器則在 `server.properties` 寫：

```properties
level-type=taiwan:taiwan
```

`level-type` 只在世界**第一次生成**時讀取——生成器連同參數會寫進 `level.dat`，之後改
`server.properties` 不會有作用。要換就開新存檔。

### 找路

```
/taiwan locate town      # 最近的聚落
/taiwan locate temple    # 最近的宮廟
/taiwan locate market    # 最近的夜市
```

聚落選址是座標的純函數，所以這個指令**不需要地圖先跑過**，也完全不碰硬碟。

## 設計上的幾個決定

**地形只有一個「地面在哪」的來源。** 填方塊、高度圖、柱體取樣、聚落選址、生態系判定全部問
`Urban.ground`。各寫一份的話它們會慢慢對不上，而那種錯誤的症狀不是報錯，是玩家掉進地板。

**山脈是「摺」出來的，不是「疊」出來的。** 直接拿 fBm 當高程只會得到一堆饅頭。脊狀雜訊把
雜訊摺一次，摺痕落在零交越線上——那條線連續、細長、會分岔，那正是山脈的形狀。

**建築沿街排，不是填滿街廓。** 座標系是「離最近的街緣幾格」而不是「街廓內的第幾格」，
所以主幹道變寬時騎樓會自己退後。

**騎樓是不能省的那一項。** 二樓以上壓在人行道上方、一樓退進去三格、每隔一戶一根柱子。
少了它，同一排房子會讀成任何一個亞洲城市。

**招牌會發光。** 台灣街景的一半是入夜之後那些亮著的招牌。招牌文字走
`ChunkAccess.setBlockEntityNbt` 的延遲路徑——世界生成階段還沒有 `Level`，直接建
`SignBlockEntity` 再 `setText` 會 NPE。

**店名一律原創惡搞，不使用任何真實商標**（見 `ShopName.java`）。

**聚落核心區塊跳過原版生態系裝飾。** 否則樹會長在四樓的頂樓加蓋上。代價是聚落正下方沒有
礦脈——礦與樹在原版是同一個裝飾階段，介面沒有分開的餘地。

## 網路相容性

Phase 1 的地形與聚落**不需要客戶端裝模組**：`CHUNK_GENERATOR` 與 `BIOME_SOURCE` 都不在
`RegistryDataLoader.SYNCHRONIZED_REGISTRIES` 裡，客戶端收到的只有算好的方塊，而生態系用的
全是原版 id。

**台灣獼猴一加進來，這件事就結束了**——`ENTITY_TYPE` 是同步的 registry。目前的版本客戶端
必須裝模組。

## 設定

地形與聚落的參數寫在 world preset（`data/taiwan/worldgen/world_preset/taiwan.json`）的
`settings` 裡，改完開新世界生效。

參數放在 world preset 而不是 config，是因為地形是**一個世界的性質**，不是一台伺服器的偏好。
放 config 的話，同一個存檔換一台機器開就會長出對不上的地形，接縫是一道垂直的斷崖。

建築類型用權重開關，**設成 0 就是關掉那一類**：

```json
"buildings": {
  "drink_shop": 3,
  "temple": 1,
  "market": 2,
  "tenement": 8,
  "convenience": 2
}
```

## 素材

`assets/taiwan/textures/entity/macaque.png` 目前是程式產生的暫用純色貼圖，64×64。
UV 配置見 `MacaqueModel.createBodyLayer()`：

| 部位 | texOffs | 方塊尺寸 | 佔用區域 |
|---|---|---|---|
| head | (0, 0) | 7×7×7 | (0,0)–(28,14) |
| face | (28, 0) | 4×4×2 | (28,0)–(40,6) |
| body | (0, 16) | 6×6×10 | (0,16)–(32,32) |
| tail | (34, 16) | 2×2×10 | (34,16)–(58,28) |
| arm | (0, 32) | 2×8×2 | (0,32)–(8,42) |
| leg | (10, 32) | 2×7×2 | (10,32)–(18,41) |

獼猴的叫聲目前借用狐狸的音效，待替換。

## 建置

```
./gradlew build
./gradlew runClient
./gradlew runServer
```

## 授權

CC0-1.0。
