import { useAuth } from "../auth/AuthContext";

export default function ProfileScreen() {
  const { profile } = useAuth();
  if (!profile) return null;
  const { stats } = profile;
  const nation = profile.nation?.toLowerCase();
  const rows: [string, number][] = [["Partidas", stats.won + stats.tied + stats.lost], ["Victorias", stats.won], ["Derrotas", stats.lost]];
  return (
    <section className="profile" data-nation={nation} aria-label="Perfil">
      <div className="profile-head">
        {nation && <svg viewBox="0 0 48 48" aria-hidden="true"><use href={`/assets/sprite.svg#emb-${nation}`} /></svg>}
        <h2>{profile.name}</h2>
      </div>
      <dl className="profile-stats">
        {rows.map(([t, n]) => <div key={t}><dt>{t}</dt><dd>{n}</dd></div>)}
      </dl>
      <a href="#/">Volver al lobby</a>
    </section>
  );
}
