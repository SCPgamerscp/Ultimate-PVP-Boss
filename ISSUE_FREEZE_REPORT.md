# [Issue] Modパック環境での戦闘中、数十秒〜数分で画面が完全停止（無音・写真のようにフリーズ）する問題

## 1. 概要
Minecraft 1.20.1 (Forge 47.3.0) 環境において、本Mod「Ultimate PVP Boss」のボス（The Legend / `PvpBossEntity`）と戦闘を行っていると、開始から数十秒〜数分経過した時点で、**突然画面が「1枚の写真」のように完全に静止し、同時にゲーム内の音（BGM・SE）も瞬時に完全無音（フリーズ）** になる。
クラッシュではないため、`crash-reports/` にレポートは一切出力されない。

---

## 2. 確定している重要事実（テスト結果）

1. **停止の性質**:
   - メモリリークやFPS低下のように徐々に重くなるのではなく、**突然急にピタッと画面が止まり、音も瞬時に完全に消える**。
2. **サーバー側（Server Thread）は正常に動いている**:
   - 画面が写真停止した後も、内部サーバーは裏で毎秒20回正常にTick処理を継続している。
   - 停止しているのは **「クライアント側（Render Thread / OpenGL / GLFWウィンドウメッセージループ）」** のみ。
3. **プレイヤーの死亡が原因ではない**:
   - クリエイティブモード（HP満タン・生存状態）でテストした際も全く同様に画面停止が発生した。
4. **プレイヤー側の操作・装備は無関係**:
   - プレイヤーが武器を振らず、魔法も使わず、ただ逃げ回る・見ているだけでも、一定時間で勝手に固まる。
5. **地形破壊（Griefing）は無関係**:
   - ボスの地形破壊・ブロック設置（`mineToward`、溶岩・クモの巣設置、クリスタル・アンカー爆破）をコンフィグで完全OFFにした状態でも同様に固まる。
6. **ボスとの距離は無関係**:
   - 至近距離（近接戦闘）でも、遠距離（逃走・射撃）でも、両方の状況で同様に固まる。
7. **ToroHealth および Embeddium は無関係（無罪確定）**:
   - **`ToroHealth` および `Embeddium`（Sodium）を完全にModフォルダから抜いた状態**でテストを実施したが、全く同様に写真停止（無音フリーズ）が発生した。
8. **環境の差異**:
   - **Vanilla（他Modなしの単体環境）**: 正常に動作し、フリーズは一切発生しない。
   - **本Modパック環境（Epic Fight等多数のMod導入）**: 必ず一定時間で画面が写真停止する。

---

## 3. 調査中に出たスタックトレース（誤誘導）
報告されたログの末尾付近には、`Render thread`（描画スレッド）から以下の警告が出ていた。これはテクスチャ読み込み失敗時の例外スタックであり、**フリーズ中に採取したスレッドダンプではない**：

```text
[Render thread/WARN]: Failed to load texture: minecraft:textures/entity/steve.png
java.io.FileNotFoundException: minecraft:textures/entity/steve.png
	at net.minecraft.client.renderer.texture.SimpleTexture$TextureImage.m_118155_(SimpleTexture.java:83)
	at net.minecraft.client.renderer.texture.SimpleTexture.m_6335_(SimpleTexture.java:58)
	at net.minecraft.client.renderer.texture.SimpleTexture.m_6704_(SimpleTexture.java:29)
	at net.minecraft.client.renderer.texture.TextureManager.m_118515_(TextureManager.java:96)
	at net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer.renderModel(HumanoidArmorLayer.java:109)
	at net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer.m_117118_(HumanoidArmorLayer.java:67)
	at net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer.m_6494_(HumanoidArmorLayer.java:44)
	at net.minecraft.client.renderer.entity.LivingEntityRenderer.m_7392_(LivingEntityRenderer.java:131)
	at net.minecraft.client.renderer.entity.EntityRenderDispatcher.m_114384_(EntityRenderDispatcher.java:140)
	at net.minecraft.client.renderer.LevelRenderer.m_109517_(LevelRenderer.java:1440)
	at net.minecraft.client.renderer.GameRenderer.m_109089_(GameRenderer.java:1126)
	at net.minecraft.client.Minecraft.m_91383_(Minecraft.java:1146)
```

当初は防具描画（`HumanoidArmorLayer`）やスキンパスを原因と見なしたが、後のウォッチドッグ採取では **この経路は停止していなかった**。

---

## 4. 環境情報
- **Minecraft**: 1.20.1
- **Forge**: 47.3.0
- **主な導入Mod**:
  - `epic-fight-20.14.17-mc1.20.1-forge.jar`（および WeaponsOfMiracles, epic_fight_avalon 等）
  - `Pehkui-3.8.2+1.20.1-forge.jar`
  - `player-animation-lib-forge-1.0.2-rc1+1.20.jar`
  - `CustomNPCs-1.20.1-GBPort-Unofficial-1.20.1.20260711.jar`
  - `AttributeFix-Forge-1.20.1-21.0.5.jar`
  - `sculkhorde-1.20.1-0.12.7.jar`
  - `L_Enders_Cataclysm-3.31.jar`

---

## 5. 確定した原因（トライデント）

ウォッチドッグが 10 / 20 / 31 秒で採取した3件のスレッドダンプは、いずれもクライアント側の

`ThrownTrident.tick` → `AbstractArrow.findHitEntity` → エンティティ衝突クエリ

で止まっていた。防具描画ではなかった。

Modパック環境では、Loyalty トライデントの帰還・連続ヒット判定が他Modのエンティティ衝突フック（Pehkui 等）を繰り返し呼び、描画スレッドを詰まらせる。サーバーは動き続け、クラッシュレポートも出ない。

---

## 6. 残した修正 / 戻した試行錯誤（2026-09-09）

### 残したもの（トライデント本体）

- 9/1 の Loyalty 回収: ボス付近に溜まった帰還トライデントを破棄する（ラグ蓄積防止）。
- 専用 `BossTridentEntity`: バニラのダメージ・Loyalty・Channeling・見た目は維持したまま、クライアント側のエンティティ衝突クエリと Loyalty 帰還中のクエリを行わない。サーバーは tick あたり最大1回だけ衝突判定する。
- プレイヤーや他Modのトライデントは変更しない。

### 戻したもの（フリーズ対策として入れた試行錯誤）

原因がトライデントと判明したため、以下はすべて元に戻した。

- 専用 `BossArmorLayer`（エンチャントの光を消して `HumanoidArmorLayer` を回避していた）
- 描画停止ウォッチドッグと診断テスト
- スキンダウンロードの単一スレッド化・タイムアウト・失敗キャッシュ
- デフォルトスキンを `DefaultPlayerSkin` に差し替えていた変更
- 毎tickの視線・装備・経路計算の間引き
- エンティティ追跡距離 128チャンク → 10チャンク、同期間隔 1 → 2

防具は通常の `HumanoidArmorLayer` に戻し、ネザライトのエンチャント光も再表示する。戦闘AI・所持数・装備効果はフリーズ対策前と同じ。

### 導入時の注意

専用トライデントはエンティティ登録が増えている。**クライアントとサーバーの両方**で JAR を入れ替える。既存ワールドに残ったバニラ `ThrownTrident` は移行しない。
