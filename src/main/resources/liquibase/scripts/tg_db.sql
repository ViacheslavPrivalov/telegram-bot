-- liquibase formatted sql

-- changeset vPrivalov:1
create table notification_task
(
    id bigSerial primary key,
    user_id bigInt not null,
    text text not null,
    date timestamp not null
);