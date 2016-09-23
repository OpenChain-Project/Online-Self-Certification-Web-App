CREATE SEQUENCE seq_section_id START 1;
CREATE TABLE section
(
  id bigint primary key default nextval ('seq_section_id'),
  name character varying(40),
  title text,
);

CREATE SEQUENCE seq_question_id START 1;
CREATE TABLE question
(
  id bigint primary key default nextval ('seq_question_id'),
  name character varying(40),
  question text,
  type character varying(120),
  correct_answer text,
  evidence_prompt text,
  evidence_validation text,
  section_id bigint,
  CONSTRAINT fk_question_section FOREIGN KEY (section_id)
      REFERENCES section (id)
);
CREATE INDEX fki_question_section
  ON public.question
  USING btree
  (section_id);
CREATE SEQUENCE seq_user_id START 1;
create table openchain_user
(
    id bigint primary key default nextval ('seq_user_id'),
    username text,
    password_token text,
    name text,
    address text,
    email text,
    verified boolean,
    passwordReset boolean,
    admin boolean,
    verificationExpirationDate date,
    uuid text,
    organization text
);
