
create table UPDATE_LOG
(
    ID             INTEGER auto_increment primary key,
    HOST_ID        INTEGER not null,
    IPV4           CHARACTER VARYING(15),
    IPV6           CHARACTER VARYING(39),
    CHANGED        TIMESTAMP default now() on update CURRENT_TIMESTAMP,
    CHANGED_UPDATE TIMESTAMP,
    STATUS         ENUM ('success', 'failed'),
    constraint HOST_ID___FK
        foreign key (HOST_ID) references HOST on delete cascade
);
