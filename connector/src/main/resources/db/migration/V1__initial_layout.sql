CREATE ALIAS MD5 FOR "org.apache.commons.codec.digest.DigestUtils.md5Hex(java.lang.String)";

CREATE TABLE unique_repository_names
(
    name VARCHAR(100),
    primary key(name)
);

CREATE TABLE sql_repositories
(
    name VARCHAR(100),
    url VARCHAR(1024) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    dialect VARCHAR(20) NOT NULL,
    primary key(name)
);

ALTER TABLE sql_repositories ADD FOREIGN KEY (name) REFERENCES unique_repository_names(name);
