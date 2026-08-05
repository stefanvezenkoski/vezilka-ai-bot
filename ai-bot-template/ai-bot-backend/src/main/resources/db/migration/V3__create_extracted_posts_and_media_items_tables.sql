create table extracted_posts (
    id bigserial primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    session_id bigint not null references extraction_sessions(id) on delete cascade,
    external_id varchar(255),
    author_handle varchar(255),
    content text,
    source_url varchar(2048),
    posted_at timestamp,
    macedonian_confidence double precision,
    version bigint not null
);

create table media_items (
    id bigserial primary key,
    post_id bigint not null references extracted_posts(id) on delete cascade,
    type varchar(255) not null,
    source_url varchar(2048) not null,
    storage_path varchar(2048)
);

create index idx_extracted_posts_session on extracted_posts(session_id);
