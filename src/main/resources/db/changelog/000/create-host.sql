create table HOST
(
    ID        INTEGER auto_increment primary key,
    NAME      CHARACTER VARYING(112) not null,
    ZONE_ID   INTEGER                not null,
    API_TOKEN CHARACTER VARYING(20)  not null,
    CHANGED   TIMESTAMP default now() on update CURRENT_TIMESTAMP,
    constraint HOST_UNIQUE
        unique (ZONE_ID, NAME),
    constraint HOST_ZONE_ID___FK
        foreign key (ZONE_ID) references ZONE  on delete cascade
);

comment on column HOST.NAME is 'prefix of the full host name';
