	CREATE SEQUENCE seq_spec_id START 1;
	alter SEQUENCE seq_spec_id OWNER TO openchain; 
	CREATE TABLE spec
	(
	    id bigint primary key default nextval ('seq_spec_id'),
	    version text
	);
	alter TABLE spec OWNER TO openchain;
	
	CREATE SEQUENCE seq_section_id START 1;
	alter SEQUENCE seq_section_id OWNER TO openchain; 
	CREATE TABLE section
	(
	  id bigint primary key default nextval ('seq_section_id'),
	  name text,
	  title text,
	  spec_version bigint,
	  CONSTRAINT fk_section_version FOREIGN KEY (spec_version)
	      REFERENCES spec (id)
	);
	alter TABLE section OWNER TO openchain;
	
	CREATE SEQUENCE seq_question_id START 1;
	alter SEQUENCE seq_question_id OWNER TO openchain;
	CREATE TABLE question
	(
	  id bigint primary key default nextval ('seq_question_id'),
	  number text,
	  question text,
	  type text,
	  correct_answer text,
	  evidence_prompt text,
	  evidence_validation text,
	  section_id bigint,
	  CONSTRAINT fk_question_section FOREIGN KEY (section_id)
	      REFERENCES section (id),
	  subquestion_of bigint,
	  CONSTRAINT fk_question_subquestion_of FOREIGN KEY (subquestion_of)
	      REFERENCES question (id),
	  spec_reference text
	);
	alter TABLE question OWNER TO openchain;
	CREATE INDEX fki_question_section
	  ON public.question
	  USING btree
	  (section_id);
	  
	CREATE SEQUENCE seq_user_id START 1;
	alter SEQUENCE seq_user_id OWNER TO openchain;
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
	alter TABLE openchain_user OWNER TO openchain;
	
	CREATE SEQUENCE seq_response_id START 1;
	alter SEQUENCE seq_response_id OWNER TO openchain;
	create table survey_response
	(
	    id bigint primary key default nextval ('seq_response_id'),
	    user_id bigint,
	    CONSTRAINT fk_response_user FOREIGN KEY (user_id)
	      REFERENCES openchain_user (id),
	    spec_version bigint,
	    CONSTRAINT fk_response_version FOREIGN KEY (spec_version)
	      REFERENCES spec (id),
	    submitted boolean
	 );
	 alter TABLE survey_response OWNER TO openchain;
	    
	CREATE SEQUENCE seq_answer_id START 1;
	alter SEQUENCE seq_answer_id OWNER TO openchain;
	create table answer
	(
	    id bigint primary key default nextval ('seq_answer_id'),
	    response_id bigint,
	    CONSTRAINT fk_answer_response FOREIGN KEY (response_id)
	      REFERENCES survey_response (id),
	    question_id bigint,
	    CONSTRAINT fk_answer_question FOREIGN KEY (question_id)
	      REFERENCES question (id),
	    answer text,
	    evidence text,
	    subanswer_of bigint,
	    CONSTRAINT fk_answer_subanswer FOREIGN KEY (subanswer_of)
	      REFERENCES answer (id)
	 );
	 alter TABLE answer OWNER TO openchain; 