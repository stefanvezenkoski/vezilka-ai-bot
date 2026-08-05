create table extraction_sessions (
    id bigserial primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    social_network varchar(255) not null,
    status varchar(255) not null,
    description varchar(255),
    started_at timestamp,
    finished_at timestamp
);

create table extraction_targets (
    id bigserial primary key,
    session_id bigint not null references extraction_sessions(id) on delete cascade,
    type varchar(255) not null,
    value varchar(255) not null
);
