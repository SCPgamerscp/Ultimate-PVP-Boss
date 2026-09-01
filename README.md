# Ultimate PVP Boss

Minecraft **Forge 1.20.1** mod. A player-skinned PvP champion with a boss bar, netherite kit, and high-level combat AI.

日本語は下。

Combat ideas were studied from [pvp-bot-fabric](https://github.com/Stepan1411/pvp-bot-fabric) (Fabric 1.21, All Rights Reserved). **No code was copied.** This project is original Java for Forge 1.20.1.

## Requirements

- Minecraft 1.20.1
- Forge 47.3.0+
- Java 17

## Build

```bash
./gradlew build
```

The jar lands in `build/libs/`. Drop it into the server or client's `mods` folder.

If `gradlew` is missing, install Gradle 8.8 and run `gradle wrapper` then `gradle build`.

## How the boss appears

Kill **10 villagers**. The champion spawns near that player, targets them, and hunts without a leash distance.

Creative test: `/pvpboss spawn`

## Kit

Enchanted netherite armor (Protection / Blast Protection mix), Sharpness V Fire Aspect II sword, shield-break axe, Power V Flame bow, Piercing IV crossbow, Loyalty III Channeling trident, 7 Totems of Undying.

## Combat

- Jump crits with the netherite sword; swaps to axe when you block
- Enchanted bow when you back off
- Five preloaded Piercing crossbow shots if you hold a shield at range
- End crystals on obsidian and charged respawn-anchor detonations
- Cobweb, then lava bucket
- Splash Poison II at point-blank, then a honey bottle to cleanse
- Low HP: Instant Health, Regeneration, enchanted golden apple — pearls and Speed II while recovering
- Half HP: Strength II + Turtle Master
- Up to **7** totems
- Loyalty + Channeling trident volleys
- Speed potions and ender pearls; chase does not stop
- Places, mines, and pillaring
- Aggros mobs that hit it

## Rewards

On death, a chest with **1000 enchanted golden apples** and **200 diamond blocks**.

## Commands (OP 2)

| Command | Effect |
| --- | --- |
| `/pvpboss spawn [player]` | Summon |
| `/pvpboss name <name>` | Rename nearby boss |
| `/pvpboss skin <username>` | Use that Minecraft skin |
| `/pvpboss remove` | Despawn nearby bosses |
| `/pvpboss kills [player]` | Villager-kill progress |

Skin and name are also in `config/ultimatepvpboss-common.toml`.

## Config highlights

`bossName`, `skinUsername`, `maxHealth` (default 40), `totemCount` (7), `villagerKillsToSpawn` (10), `allowGrief`, `gappleReward`, `diamondBlockReward`.

---

# Ultimate PVP Boss（日本語）

**Forge 1.20.1** 用。スキン変更可能なプレイヤー型ボス。ボスバー付き。村人を10人倒すと出現する。

[pvp-bot-fabric](https://github.com/Stepan1411/pvp-bot-fabric) は戦闘の参考のみ。コードは移植していない。

## 出現

村人を **10人** 倒すと、そのプレイヤー付近に伝説級PvPチャンピオンが現れる。逃げてもエンダーパールと移動速度上昇で追う。

テスト: `/pvpboss spawn`

## 報酬

死亡時、チェストにエンチャント金リンゴ **1000** 個とダイヤブロック **200** 個。
