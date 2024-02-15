create table ZONE
(
    ID      INTEGER auto_increment primary key,
    NAME    CHARACTER VARYING(512) not null,
    NS      CHARACTER VARYING(512) not null,
    CHANGED TIMESTAMP default now() on update CURRENT_TIMESTAMP
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
    constraint HOST_PK
        primary key (ID),
    constraint HOST_UNIQUE
        unique (ZONE_ID, NAME),
    constraint HOST_ZONE_ID_FK
        foreign key (ZONE_ID) references ZONE
);

comment on column HOST.NAME is 'prefix of the full host name';



