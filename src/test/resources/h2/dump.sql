DROP ALL OBJECTS ;

CREATE USER IF NOT EXISTS "DBA" SALT '913deda40286c1fc' HASH '969ac3e0877b5908dee9bbcba42360ef2b4425869097110bae060c44cad56682' ADMIN;
CREATE TABLE "ZONE"
(
    "ID"      INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1 RESTART WITH 3) NOT NULL,
    "NAME"    CHARACTER VARYING(512)                                                 NOT NULL,
    "NS"      CHARACTER VARYING(512) COMMENT 'the name server of the zone'           NOT NULL,
    "CHANGED" TIMESTAMP DEFAULT LOCALTIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE "ZONE"
    ADD CONSTRAINT "CONSTRAINT_2" PRIMARY KEY ("ID");
ALTER TABLE ZONE
    ADD CONSTRAINT "zone-unique" UNIQUE ("NAME");
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.ZONE;     
INSERT INTO "ZONE"
VALUES (1, 'dynhost0.info', 'ns0.domain.info', TIMESTAMP '2024-01-28 12:00:37.013707');
INSERT INTO "ZONE"
VALUES (2, 'dynhost1.info', 'ns1.domain.info', TIMESTAMP '2024-01-28 12:00:37.013707');
CREATE UNIQUE NULLS DISTINCT INDEX "UNIQUE_IDX" ON "ZONE"("NAME" NULLS FIRST, "NS" NULLS FIRST);
CREATE TABLE "HOST"
(
    "ID"        INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1 RESTART WITH 5) NOT NULL,
    "NAME"      CHARACTER VARYING(112) COMMENT 'prefix of the host'                    NOT NULL,
    "ZONE_ID"   INTEGER                                                                NOT NULL,
    "API_TOKEN" CHARACTER VARYING(20)                                                  NOT NULL,
    "CHANGED"   TIMESTAMP DEFAULT LOCALTIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
ALTER TABLE "HOST"
    ADD CONSTRAINT "HOST_PK" PRIMARY KEY ("ID");
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.HOST;
INSERT INTO "HOST"
VALUES (1, 'my0', 1, '1234567890abcdef', TIMESTAMP '2024-01-28 12:06:29.821934');
INSERT INTO "HOST"
VALUES (2, 'test0', 1, '1234567890abcdex', TIMESTAMP '2024-01-28 12:06:29.821934');
INSERT INTO "HOST"
VALUES (3, 'my1', 2, '1234567890abcdef', TIMESTAMP '2024-01-28 12:06:29.821934');
INSERT INTO "HOST"
VALUES (4, 'test1', 2, '1234567890abcdex', TIMESTAMP '2024-01-28 12:06:29.821934');
ALTER TABLE "HOST"
    ADD CONSTRAINT "HOST_UNIQUE" UNIQUE ("ZONE_ID", "NAME");
ALTER TABLE "HOST"
    ADD CONSTRAINT "ZONE_ID___FK" FOREIGN KEY ("ZONE_ID") REFERENCES "ZONE" ("ID") NOCHECK;

CREATE TABLE "UPDATE_LOG"
(
    "ID"           INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 1) NOT NULL,
    "HOST_ID"      INTEGER,
    IPV4           CHARACTER VARYING(15),
    IPV6           CHARACTER VARYING(39),
    CHANGED        TIMESTAMP default now() on update CURRENT_TIMESTAMP,
    CHANGED_UPDATE TIMESTAMP,
    "STATUS"       ENUM ('success', 'failed')
);

ALTER TABLE "UPDATE_LOG"
    ADD CONSTRAINT "HOST_ID___FK" FOREIGN KEY ("HOST_ID") REFERENCES "HOST" ("ID") NOCHECK