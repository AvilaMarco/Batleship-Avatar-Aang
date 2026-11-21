alter table game_player drop column emote;
drop table ship_salvos_impacted;
alter table player_roles drop constraint player_roles_roles_check;
alter table player_roles alter column roles type varchar(20)
    using (case roles when 0 then 'GUEST' when 1 then 'PLAYER' when 2 then 'ADMIN' end);
alter table player_roles add constraint player_roles_roles_check check (roles in ('GUEST', 'PLAYER', 'ADMIN'));
