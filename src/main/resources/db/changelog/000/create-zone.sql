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