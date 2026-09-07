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

## 3. フリーズ付近のログに記録されたスタックトレース
報告されたログの末尾付近には、`Render thread`（描画スレッド）から以下の警告が出ている。これはテクスチャ読み込み失敗時の例外スタックであり、フリーズ中に採取したスレッドダンプではない：

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

この記録から確認できるのは、**防具描画の呼び出し経路で存在しないスキンテクスチャが要求されたこと**まで。`HumanoidArmorLayer.renderModel` がハングしていたこと、Glintや特定Modが原因であることは、これだけでは確定できない。停止後に複数回のスレッドダンプを採取して判断する必要がある。

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

## 5. 調査・解決すべき最有力容疑

1. **`HumanoidArmorLayer` に対する他Modの Mixin / ボーンフックの競合**:
   - ボスはネザライト防具一式（フルエンチャント）を常時装備している。
   - 人型モデルや防具描画に介入するModとの競合を調査する。ただし、列挙した各Modの当該バージョンがこの経路へどのように介入しているかは、実際のMixin適用情報・停止中のスタックで確認する必要がある。
   - ボスの特定のアクション（ジャンプ、腕振り、移動）の連続により、防具レイヤーの描画バッファまたは行列計算がデッドロックを起こしている可能性。
2. **`BossSkinTexture` のバニラテクスチャパス指定ミス**:
   - `BossSkinTexture.java` 内で `new ResourceLocation("textures/entity/steve.png")` が指定されているが、Minecraft 1.20.1 にこのパスは存在しない（バニラは `textures/entity/player/wide/steve.png` または `DefaultPlayerSkin.getDefaultSkin()`）。
   - パスの誤りは具体的な不具合。ただし、VanillaのTextureManagerは失敗時のmissing textureも登録するため、この警告だけから「毎フレーム再読込」「バッファ圧迫」を断定することはできない。
3. **ボスのエンティティトラッキングとパケット同期**:
   - バニラのエンティティ同期と他Modのアニメーション同期が競合している可能性。

---

## 6. 今回の対策と検証状況（2026-09-07）

### 比較した実装

[darkdoppelganger のレンダラー](https://github.com/Bandit-bytes/darkdoppelganger/blob/master/src/main/java/net/bandit/darkdoppelganger/entity/renderer/DarkDoppelgangerRenderer.java) は `AbstractSpellCastingMobRenderer` を継承しており、こちらのように `HumanoidArmorLayer` を直接登録していない。スキンも `PlayerInfo` / `AbstractClientPlayer` と `DefaultPlayerSkin` を利用している。

見た目や機能が似ていても描画経路は同一ではない。この差を参考にしたが、相手ModのコードのコピーやIron's Spellbooks / GeckoLib等の新規依存追加は行っていない。この比較だけで原因Modを特定したわけではない。

### 実装した変更

- `BossSkinTexture` と `PvpBossRenderer` のデフォルトスキンを `DefaultPlayerSkin.getDefaultSkin()` に変更。Steve指定時、カスタムスキン待機中、取得失敗時とも存在する組み込みスキンを使用する。
- ボス固有の `BossArmorLayer` を `RenderLayer` から実装。`HumanoidArmorLayer` の継承や呼び出しを避け、固定ネザライト装備を通常の防具テクスチャと単一の描画バッファで表示する。
- 防具の汎用モデル・テクスチャフックとGlint用の複合バッファ経路を、このボスの防具描画から外す。全ModのMixinやGPU停止を防ぐ保証ではない。
- **防具のエンチャントの光る表示は省略するが、装備・エンチャント効果・防御力・AI・所持数は変更しない。** 手持ちアイテムの描画は従来どおり。固定装備用のため、外部コマンド等で置換したネザライト以外の防具・防具トリム・独自防具モデルはこのレイヤーでは表示しない。
- 描画停止の自動診断を追加。ボスを一度描画したワールドでは、描画のheartbeatが10秒途絶えると、独立したデーモンスレッドが `[PVPBOSS-RENDER-STALL]` を付けて全Javaスレッドのスタック・状態・ロック所有者をログに出す。同じ停止は約10秒間隔で最大3回まで。描画再開で上限をリセットし、ワールドを離れると監視を解除する。
- 診断はクライアント限定のForgeイベントで動作し、描画スレッドへタスクを送信したり、そこでスレッドダンプを取得・待機したりしない。ポーズ中でも描画が継続すれば停止とは判定しない。ただし長いリソース再読み込み・OSによる中断でも検知する可能性がある。

### 検証できたこと／できていないこと

- Java 17で `RenderStallWatchdogTest` の3件の回帰テストに合格。通常動作・未監視時の無出力、10秒閾値、出力間隔と上限、復帰と再監視、深いスタックを省略しないこと、停止した模擬描画スレッドの採取、監視スレッドが単一デーモンであることを確認した。
- `gradlew build` / `check` からこのテストを実行する `renderStallRegressionTest` タスクを追加した。
- **Forge全体のビルドは未完了。** 約1GBの作業環境でMinecraftのremapping前処理中に停止し、JVMメモリ・並列数を制限しても完了できなかった。GitHub Actionsでの代替ビルドも、接続中GitHub Appの `workflows` 権限不足で設定をpushできず、CI設定はPRから除外した。
- **実Modパックでの動作・フリーズ解消は未検証。** 上記の単体テストは防具のOpenGL描画や他Modとの互換性を検証するものではない。今回の変更は、確定したスキンパス不具合の修正と、疑わしい防具描画経路の回避・再発時の証拠採取である。

### 手元での確認手順

1. PRのソースを取得し、Java 17と十分な空きメモリのある環境で `./gradlew build`（Windowsでは `.\gradlew.bat build`）を実行する。成功した場合のみ `build/libs/ultimatepvpboss-1.0.0.jar` を使用する。同名の旧版JARと混同しないこと。
2. ワールドをバックアップし、古いUltimate PVP BossのJARを外して新しいJARを配置する。描画修正なので、**サーバーだけでなく遊ぶクライアントにも新しいJARが必要**。
3. Modパック構成を維持し、まず `allowGrief=false`、Steveスキン、`/pvpboss spawn` で10分以上確認する。近接・遠距離、クリエイティブ・サバイバル、ボス複数体を確認し、その後カスタムスキンと通常設定でも確認する。
4. 再発した場合は強制終了する前に30〜40秒待ち、`logs/latest.log` の `[PVPBOSS-RENDER-STALL]` から始まる3回分を保存する。ログ全体と使用したJAR・Mod一覧を添える。これは自動復旧機能ではなく原因を絞るための診断機能。
5. JVM全体の停止・ログ出力自体の停止などで自動記録が出ない場合は、同じユーザー権限のJDK 17で `jcmd -l` からMinecraftのPIDを探し、`jcmd <PID> Thread.print -l > freeze-1.txt` を実行する。数秒空けて `freeze-2.txt`、`freeze-3.txt` にも採取する。外部ダンプも採取できない場合は、その状況も報告する。

診断テストだけなら、Minecraftを起動せず以下で実行できる（Java 17、リポジトリ直下）：

```sh
mkdir -p build/watchdog-test
javac --release 17 -d build/watchdog-test src/main/java/com/ailingmeng/ultimatepvpboss/client/RenderStallWatchdog.java src/test/java/com/ailingmeng/ultimatepvpboss/client/RenderStallWatchdogTest.java
java -cp build/watchdog-test com.ailingmeng.ultimatepvpboss.client.RenderStallWatchdogTest
```
