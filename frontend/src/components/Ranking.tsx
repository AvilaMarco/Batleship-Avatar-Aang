import type { RankingEntry } from "../api/types";

export default function Ranking({ entries }: { entries: RankingEntry[] }) {
  return (
    <section aria-labelledby="ranking-title">
      <h2 id="ranking-title">Ranking</h2>
      <table>
        <thead>
          <tr><th>Jugador</th><th>Puntos</th><th>Ganadas</th><th>Empatadas</th><th>Perdidas</th></tr>
        </thead>
        <tbody>
          {entries.map((e) => (
            <tr key={e.player.id}>
              <td>{e.player.name}</td><td>{e.stats.score}</td><td>{e.stats.won}</td>
              <td>{e.stats.tied}</td><td>{e.stats.lost}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
