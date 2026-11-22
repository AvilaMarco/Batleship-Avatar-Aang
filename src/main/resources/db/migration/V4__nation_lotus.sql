-- Expand-only: ensancha el check para admitir LOTUS; los valores previos siguen validos.
alter table game drop constraint game_nation_check;
alter table game add constraint game_nation_check check (nation in ('WATER','AIR','FIRE','EARTH','LOTUS'));
alter table player drop constraint player_nation_check;
alter table player add constraint player_nation_check check (nation in ('WATER','AIR','FIRE','EARTH','LOTUS'));
