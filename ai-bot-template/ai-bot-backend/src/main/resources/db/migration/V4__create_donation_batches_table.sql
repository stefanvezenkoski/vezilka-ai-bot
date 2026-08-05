create table donation_batches (
    id bigserial primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    status varchar(255) not null,
    vezilka_reference varchar(255),
    submitted_at timestamp
);

alter table extracted_posts
    add column donation_batch_id bigint references donation_batches(id) on delete set null;
