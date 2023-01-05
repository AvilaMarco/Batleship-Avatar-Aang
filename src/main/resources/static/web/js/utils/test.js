function randomNation(playerData) {
  const nations = ["FIRE", "EARTH", "AIR", "WATER"];
  const n = Math.round(Math.random() * 3);
  playerData.nation = nations[n];
}
const playerTest = {
  id: 5,
  nation: "EARTH",
  email: "marco@aaa.com",
  name: "Marco Avila",
  stats: {
    score: 3,
    won: 1,
    tied: 0,
    lost: 0,
    win_rate: 100
  }
};
export { randomNation, playerTest};
