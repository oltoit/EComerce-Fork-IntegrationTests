-- Users
-- Normal User: username: user, password: user, role: USER
insert into app_user (id, username, password, role) values (1, 'user1', '$2a$10$UcSltcvIoORWLKcCxo.4quqBWSoD0ZdC86caU..NSzNJdOdqgrKx2', 'USER');
-- Admin / Super User: username: admin, password: admin, role: ADMIN
insert into app_user (id, username, password, role) values (3, 'admin', '$2a$10$1e2I81sXJW0gJDKyDZxhc.JXXIpK1Y0T4AKp0GwNs32370Wa2bDqK', 'ADMIN');