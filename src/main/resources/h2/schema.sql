create table ZONE
(
    ID      INTEGER auto_increment primary key,
    NAME    CHARACTER VARYING(512) not null,
    NS      CHARACTER VARYING(512) not null,
    CHANGED TIMESTAMP default now() on update CURRENT_TIMESTAMP,
    constraint ZONE_UNIQUE
        unique (NAME)
);

comment on column ZONE.NS is 'the name server of the zone';

create unique index UNIQUE_IDX
    on ZONE (NAME);

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
        foreign key (ZONE_ID) references ZONE
);

comment on column HOST.NAME is 'prefix of the full host name';

create table PUBLIC.UPDATE_LOG
(
    ID             INTEGER auto_increment primary key,
    HOST_ID        INTEGER not null,
    IPV4           CHARACTER VARYING(15),
    IPV6           CHARACTER VARYING(39),
    CHANGED        TIMESTAMP default now() on update CURRENT_TIMESTAMP,
    CHANGED_UPDATE TIMESTAMP,
    STATUS         ENUM ('success', 'failed', 'new'),
    constraint HOST_ID___FK
        foreign key (HOST_ID) references PUBLIC.HOST
);


