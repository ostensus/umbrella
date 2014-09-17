create function md5(v varchar(32672)) returns varchar(32)
language java deterministic no sql
external name 'CLASSPATH:org.apache.commons.codec.digest.DigestUtils.md5Hex';