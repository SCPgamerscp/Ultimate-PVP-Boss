# Ultimate PVP Boss

Minecraft **Forge 1.20.1** mod. A player-skinned PvP champion with a boss bar, full netherite kit, realistic PvP AI, finite consumable inventory, and high-level combat tactics.

日本語の説明は[下部](#ultimate-pvp-boss日本語)にあります。

Combat ideas were studied from [pvp-bot-fabric](https://github.com/Stepan1411/pvp-bot-fabric) (Fabric 1.21, All Rights Reserved). **No code was copied.** This project is original Java for Forge 1.20.1.

## Requirements

- Minecraft 1.20.1
- Forge 47.3.0+
- Java 17

## Build

```bash
./gradlew build
# On Windows PowerShell / Command Prompt:
.\gradlew build
```

The jar lands in `build/libs/`. Drop it into the server or client's `mods` folder.

## How the Boss Appears

Kill **10 villagers**. The champion spawns near that player, targets them, and hunts relentlessly.

Creative / Admin test: `/pvpboss spawn`

## Kit & Equipment

- **Armor**: Enchanted Netherite Armor (Protection IV / Blast Protection IV mix, Unbreaking III, Mending, Soul Speed III, Depth Strider III)
- **Melee**: Netherite Sword (Sharpness V, Fire Aspect II, Knockback I, Sweeping Edge III), Netherite Axe (Efficiency V, Sharpness V - for shield breaking)
- **Ranged**: Power V Flame Bow, Piercing IV Crossbow, Loyalty III Channeling Trident
- **Tools**: Netherite Pickaxe (Efficiency V - mines player defenses)

## Realistic PvP Combat AI

- **Player Movement**: Movement speed and sprinting multipliers match vanilla player physics.
- **Realistic Placement Reach (≤ 4.5m)**:
  - **Crystal PvP**: Places obsidian and End Crystals adjacent to the player, instantly deploying an **Obsidian Blast Shield** between the boss and the crystal to block blast waves (eliminating self-damage).
  - **Anchor PvP**: Places and immediately overcharges Respawn Anchors with an **Obsidian Blast Shield** protecting the boss from the explosion.
  - **Web & Lava Combo**: Traps target's feet in cobweb followed by lava placement.
  - **Shield Counter**: Instantly swaps to Netherite Axe to disable blocking shields.
  - **Melee Crits & Strafe**: Jump critical hits with sword, circling and strafing around the target.
- **Ranged Tactics (> 4.5m)**:
  - Bow and multi-shot Piercing Crossbow against distant or shielded targets.
  - Loyalty Trident volleys (automatically catches returning tridents with zero lag).
  - Ender Pearl chases to quickly close distance.
- **Survival & Recovery**:
  - Drops back with Ender Pearls and Speed buffs when HP drops below 38%.
  - Eats Enchanted Golden Apples and drinks Healing Potions to recover.
  - Strength II and Turtle Master buffs triggered at half health.
  - Drinks honey to cure poison.
  - Pillars up with blocks if target gains high ground; mines through blocks if line of sight is obstructed.

## Finite Item Limits (Configurable)

Just like a real player, the boss has a finite inventory of supplies. When exhausted, the boss can no longer use that skill:

- **Totems of Undying**: 7 (boss dies when all are popped)
- **Enchanted Golden Apples**: 320
- **Healing Potions**: 160
- **Ender Pearls**: 320
- **End Crystals**: 1280
- **Respawn Anchors**: 320
- **Cobwebs**: 320
- **Poison Splash Potions**: 80

*Item counts are saved in NBT across world reloads and fully customizable in the config.*

## Rewards

On victory, a chest drops containing **1,000 Enchanted Golden Apples** and **200 Diamond Blocks** (configurable).

## Commands (Permission Level 2)

| Command | Effect |
| --- | --- |
| `/pvpboss spawn [player]` | Summon the boss near you or specified player |
| `/pvpboss name <name>` | Change display name of boss (saved for future spawns) |
| `/pvpboss skin <username\|url>` | Apply skin from Minecraft username or direct PNG image URL (`http://`, `https://`) (saved for future spawns) |
| `/pvpboss skin reset` | Reset boss skin to default (Steve) |
| `/pvpboss remove` | Remove/despawn nearby bosses |
| `/pvpboss kills [player]` | Check villager kill progress |

## Custom Skins & Name Customization

You can change the champion's skin using either a **Minecraft username** or a **direct PNG image URL**:

- **Command**: `/pvpboss skin <username|url>`
  - *Example (Username)*: `/pvpboss skin Notch`
  - *Example (Direct URL)*: `/pvpboss skin https://i.imgur.com/example.png`
  - *No boss in the world?* The command will automatically save the setting to your config for future boss spawns!
- **Reset to Default**: `/pvpboss skin reset` (resets to Steve)
- **Rename**: `/pvpboss name <name>` (e.g. `/pvpboss name §c§lPvP King`)

All skin and name preferences are persisted in `config/ultimatepvpboss-common.toml`.

## Configuration (`config/ultimatepvpboss-common.toml`)

- `bossName`: Default display name
- `skinUsername`: Minecraft username or direct image URL (http:// or https://) for skin
- `maxHealth`: Max health (default 40.0)
- `totemCount`: Number of Totems of Undying (default 7)
- `gappleCount`, `healPotionCount`, `pearlCount`, `crystalCount`, `anchorCount`, `webCount`, `poisonCount`: Finite consumable limits
- `villagerKillsToSpawn`: Villagers needed to summon the boss (default 10)
- `repeatSpawn`: Spawn every N kills repeatedly
- `followRange`: Target hunting range (default 128 blocks)
- `allowGrief`: Allow placing/breaking blocks, crystals, anchors, lava, cobwebs
- `channelingAlways`: Trident summons lightning even without thunderstorm
- `gappleReward`, `diamondBlockReward`: Reward amounts in victory chest
- `announce`: Broadcast spawn, totem pop, and defeat messages

---

# Ultimate PVP Boss（日本語）

Minecraft **Forge 1.20.1** 用Mod。プレイヤー型スキン、専用ボスバー、ネザライト装備、リアルなPvP AI、そして有限リソース（所持数制限）を備えた伝説級PvPチャンピオンボスを追加します。

## 必要環境

- Minecraft 1.20.1
- Forge 47.3.0 以上
- Java 17

## ビルド方法

```powershell
.\gradlew build
```

生成されたJARファイル（`build/libs/ultimatepvpboss-1.0.0.jar`）をMinecraftの `mods` フォルダに配置してください。

## 出現条件

プレイヤーが村人を **10人** 倒すと、そのプレイヤーの近くにボスが召喚され、逃げても執拗に追跡・攻撃してきます。

クリエイティブ/管理者用コマンド: `/pvpboss spawn`

## 装備

- **防具**: フルエンチャント・ネザライト装備（ダメージ軽減IV / 爆破耐性IV、耐久力III、修繕、ソウルスピードIII、水中歩行III）
- **武器**: ネザライトの剣（ダメージ増加V、火属性II、ノックバックI、範囲ダメージ増加III）、ネザライトの斧（効率強化V、ダメージ増加V - 盾破壊用）
- **射撃**: 弓（射撃ダメージ増加V、火矢）、クロスボウ（貫通IV）、トライデント（忠誠III、召雷I）
- **ツール**: ネザライトのツルハシ（効率強化V - 障害物採掘用）

## 本格的なPvP戦闘AI

- **プレイヤー準拠の移動速度**: バニラプレイヤーの歩行・ダッシュ速度と完全に同等。
- **インファイト設置リーチ（4.5m以内）**:
  - **クリスタルPvP**: 黒曜石とエンドクリスタルを相手の足元に設置し、自身とクリスタルの間に**黒曜石シールドを瞬時に配置して爆風を遮断（自爆ダメージを完全カット）**しながら起爆。
  - **アンカーPvP**: リスポーンアンカー設置時も**黒曜石シールド**で自身を保護しつつ最大チャージして大爆破。
  - **クモの巣＆マグマトラップ**: 足元をクモの巣で拘束した直後にマグマを流し込むコンボ。
  - **盾割り攻撃**: プレイヤーが盾を構えた瞬間に斧に持ち替えて盾を無効化。
  - **ジャンプクリティカル＆回り込み**: 剣でのジャンプクリティカルヒットやサークルストレイフ（左右ステップ）。
- **中〜遠距離戦（4.5m超）**:
  - 弓や貫通クロスボウ、召雷トライデントによる射撃戦（戻ってきた忠誠トライデントは自動キャッチ・消滅しラグを防止）。
  - 距離が離れた場合はエンダーパールで急速接近。
- **回復・サバイバル行動**:
  - HPが38%以下になるとパールで後退し、俊敏効果を付与してエンチャント金リンゴや治癒ポーションで回復。
  - HP半分時に「力II」および「タートルマスター」バフを発動。
  - 毒を受けた場合はハチミツを飲んで解毒。
  - 高所に逃げられた場合は足元にブロックを積んで登攀（ピラリング）、壁に隠れられた場合はツルハシで採掘して突破。

## アイテム所持数制限（有限リソース）

本物のPvPプレイヤーと同様に、各アイテムの所持数に上限があります。使い切るとその技は使用不能になります：

- **不死のトーテム**: 7 個（全て消費すると死亡）
- **エンチャント金リンゴ**: 320 個
- **治癒ポーション**: 160 個
- **エンダーパール**: 320 個
- **エンドクリスタル**: 1280 個
- **リスポーンアンカー**: 320 個
- **蜘蛛の巣**: 320 個
- **毒ポーション**: 80 個

*各残数はワールド再読み込み時も保存され、コンフィグから自由に設定可能です。*

## 勝利報酬

ボスを倒すと、チェストに **エンチャント金リンゴ 1000個** と **ダイヤブロック 200個** が出現します（個数変更可能）。

## コマンド一覧（権限レベル2）

| コマンド | 効果 |
| --- | --- |
| `/pvpboss spawn [プレイヤー名]` | ボスを召喚 |
| `/pvpboss name <名前>` | ボスの表示名を変更（次回以降のスポーンにも保存） |
| `/pvpboss skin <ユーザー名\|画像URL>` | スキンをMinecraftユーザー名または直接の画像URL（`http://`, `https://`）に変更（次回以降のスポーンにも保存） |
| `/pvpboss skin reset` | ボスのスキンをデフォルト（Steve）にリセット |
| `/pvpboss remove` | 近くのボスを消去 |
| `/pvpboss kills [プレイヤー名]` | 村人討伐数を確認 |

## スキン・名前のカスタマイズ

ボスのスキンは **Minecraftユーザー名** または **直接のPNG画像URL** で自由に変更できます：

- **スキン変更コマンド**: `/pvpboss skin <ユーザー名|画像URL>`
  - *プレイヤー名指定の例*: `/pvpboss skin Notch`
  - *画像URL指定の例*: `/pvpboss skin https://i.imgur.com/example.png`
  - *ボスがいない場合*: 自動的に設定ファイル（Config）に保存され、次回以降に出現するボスのスキンとして適用されます。
- **スキンのリセット**: `/pvpboss skin reset`（デフォルトのスティーブに戻す）
- **名前の変更**: `/pvpboss name <名前>`（カラーコード `§c§l` 等も使用可能）

設定内容は `config/ultimatepvpboss-common.toml` に永続保存されます。

## 設定ファイル (`config/ultimatepvpboss-common.toml`)

- `bossName`: デフォルトのボス名
- `skinUsername`: スキン用のプレイヤー名または画像URL (http:// または https://)
- `maxHealth`: 最大体力（初期値 40.0）
- `totemCount`: トーテムの所持数（初期値 7）
- `gappleCount`, `pearlCount`, `crystalCount` 等: 各アイテムの所持上限数
- `villagerKillsToSpawn`: 出現に必要な村人討伐数（初期値 10）
- `allowGrief`: ブロック設置・破壊・爆破の許可
- `gappleReward`, `diamondBlockReward`: 勝利チェストの報酬数

