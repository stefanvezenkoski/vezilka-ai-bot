create table bot_action_logs (
    id bigserial primary key,
    session_id bigint not null references extraction_sessions(id) on delete cascade,
    action_type varchar(255) not null,
    details text,
    successful boolean not null,
    occurred_at timestamp not null
);

create index idx_bot_action_logs_session on bot_action_logs(session_id);
