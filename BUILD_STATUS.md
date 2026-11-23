# Build status — 1.3.0 Sakura UI Refresh

- Refreshed the inventory and enchantment-screen presentation, panel artwork, slot styling, and decorative Sakura animation layer.
- This is a UI-only update. The client command `/inv`, default/configurable K key mapping, item registry catalogue, enchantment registry lookup/filtering, quantity controls, Singleplayer checks, and item-drop service were left unchanged.
- Replaced the Sakura panel and petal PNG resources while preserving their original resource paths and dimensions expected by the renderer (700×410 and 32×32).
- Static resource checks, JSON validation, and source-structure checks were performed during packaging.
- A full Gradle compile and in-game rendering test have **not** been run in this environment. Java 25 and the compatible Gradle runtime are required; use Codespaces or GitHub Actions and only treat the output as built after the workflow succeeds.
