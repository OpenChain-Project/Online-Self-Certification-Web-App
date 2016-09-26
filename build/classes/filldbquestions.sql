-- Script to load questions into the DB.
-- NOTE: This script only works if there is ONLY one version of the spec in the database (this version)  The database must be completely clean
-- Reccomend dropping the tables and recreating prior to running this script
insert into spec(version) values('1.0');
insert into section(name, title, spec_version) values('G1','Know Your FOSS Responsibilities', (select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '1.1', 
        'Do you have rules that govern FOSS license compliance of the Supplied Software distribution?',
        'YES_NO','Yes', (select id from section where name='G1'));
insert into question(number, question, type, correct_answer, section_id) values(
        '1.2', 
        'Are these rules internally communicated?',
        'YES_NO','Yes', (select id from section where name='G1'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '1.3', 
        'Are these rules documented?',
        'YES_NO','Yes', (select id from section where name='G1'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '1.4', 
        'Is your Software Staff aware of the rules that govern FOSS license compliance of the Supplied Software distribution?',
        'YES_NO','Yes', (select id from section where name='G1'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '1.5', 
        'Do you document, how you make your Software Staff aware of the existing procedures that govern FOSS license compliance of the Supplied Software distribution? ',
        'YES_NO','Yes', (select id from section where name='G1'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '1.6', 
        'Do you make your software staff aware of the existence of the FOSS policy through',
        'SUBQUESTIONS','1', (select id from section where name='G1'));        
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.6.1', 
        'Training,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.6'));                                   
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.6.2', 
        'Internal wiki,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.6'));                                   
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.6.3', 
        'Other practical communication methods?',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.6'));                                   
insert into question(number, question, type, correct_answer, section_id) values(
        '1.7', 
        'Have 85% or more of the Software Staff attended a FOSS training within the last 24 months?',
        'YES_NO','Yes', (select id from section where name='G1'));                 
insert into question(number, question, type, correct_answer, section_id) values(
        '1.8', 
        'Does this training cover the following topics:',
        'SUBQUESTIONS','5', (select id from section where name='G1'));
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.8.1', 
        'Basics of IP law pertaining to FOSS and FOSS licenses,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.8'));    
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.8.2', 
        'FOSS licensing concepts (including the concepts of permissive and copyleft licenses),',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.8'));    
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.8.3', 
        'FOSS project licensing models,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.8'));    
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.8.4', 
        'Software Staff roles and responsibilities pertaining to FOSS compliance specifically and the FOSS policy in general,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.8'));    
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.8.5', 
        'Process for identifying, recording and/or tracking of FOSS components contained in Supplied Software?',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.8'));    
 insert into question(number, question, type, correct_answer, section_id) values(
        '1.9', 
        'Do you use one or more of the following FOSS course materials:',
        'SUBQUESTIONS','1', (select id from section where name='G1'));
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.9.1', 
        'Slide decks,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.9'));                                                                                
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.9.2', 
        'Online courses,',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.9'));                                                                                
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '1.9.3', 
        'Other training material?',
        'YES_NO','Yes', (select id from section where name='G1'),(select id from question where number='1.9'));                  
insert into question(number, question, type, correct_answer, section_id) values(
        '1.10', 
        'Do you track the completion of the course for all Software Staff?',
        'YES_NO','Yes', (select id from section where name='G1'));      
insert into question(number, question, type, correct_answer, section_id) values(
        '1.11', 
        'Do you provide a written test to track the completion of the course for all Software Staff?',
        'YES_NO','Yes', (select id from section where name='G1'));      
        
insert into section(name, title, spec_version) values(
'G2','Assign Responsibility for Achieving Compliance', 
(select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '2.1', 
        'Have you assigned an individual responsible for managing internal FOSS compliance?',
        'YES_NO','Any', (select id from section where name='G2'));
insert into question(number, question, type, correct_answer, section_id) values(
        '2.2', 
        'Have you assigned a group of persons responsible of managing internal FOSS compliance?',
        'YES_NO','Any', (select id from section where name='G2'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '2.3', 
        'Is the FOSS Liaison identical with the individual responsible for managing internal FOSS compliance or part of the group of persons responsible for it?',
        'YES_NO','Any', (select id from section where name='G2'));              
insert into question(number, question, type, correct_answer, section_id) values(
        '2.4', 
        'Is the FOSS compliance management activity sufficiently resourced regarding',
        'SUBQUESTIONS','2', (select id from section where name='G2'));              
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '2.4.1', 
        'Time allocated to perform the role,',
        'YES_NO','Yes', (select id from section where name='G2'),(select id from question where number='2.4'));         
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '2.4.2', 
        'Budget allocated to the role?',
        'YES_NO','Yes', (select id from section where name='G2'),(select id from question where number='2.4'));             
insert into question(number, question, type, correct_answer, section_id) values(
        '2.5', 
        'Have you assigned responsibilities to develop and maintain FOSS compliance policy and processes?',
        'YES_NO','Yes', (select id from section where name='G2'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '2.6', 
        'Is legal expertise pertaining to FOSS compliance accessible to the FOSS Compliance Role (e.g., could be internal or external)?',
        'YES_NO','Yes', (select id from section where name='G2'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '2.7', 
        'Have you assigned individual(s) responsible for receiving external FOSS compliance inquiries (“FOSS Liaison”)?',
        'YES_NO','Yes', (select id from section where name='G2'));                  
insert into question(number, question, type, correct_answer, section_id) values(
        '2.8', 
        'Is the FOSS Liaison function publicly identified in one of the following ways:',
        'SUBQUESTIONS','1', (select id from section where name='G2'));              
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '2.8.1', 
        'Email address?',
        'YES_NO','Yes', (select id from section where name='G2'),(select id from question where number='2.8'));           
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '2.8.2', 
        'Linux Foundation''s Open Compliance Directory?',
        'YES_NO','Yes', (select id from section where name='G2'),(select id from question where number='2.8'));           
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '2.8.3', 
        'Another practical way?',
        'YES_NO','Yes', (select id from section where name='G2'),(select id from question where number='2.8'));    
insert into question(number, question, type, correct_answer, section_id) values(
        '2.9', 
        'Can third parties reach the FOSS Liaison by way of electronic communication?',
        'YES_NO','Yes', (select id from section where name='G2'));    
insert into question(number, question, type, correct_answer, section_id) values(
        '2.10', 
        'Does the FOSS Liaison respond to FOSS compliance inquiries?',
        'YES_NO','Yes', (select id from section where name='G2'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '2.11', 
        'Does the FOSS Liaison make commercially reasonable efforts to respond to FOSS compliance inquiries as appropriate?',
        'YES_NO','Yes', (select id from section where name='G2'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '2.12', 
        'Can the FOSS Liaison escalate FOSS compliance issues to resolve them?',
        'YES_NO','Yes', (select id from section where name='G2'));            
                        
insert into section(name, title, spec_version) values(
'G3','Review and Approve FOSS Content', 
(select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '3.1', 
        'Do you identify all FOSS components and their respective Identified Licenses from which Supplied Software is comprised?',
        'YES_NO','Yes', (select id from section where name='G3'));
insert into question(number, question, type, correct_answer, section_id) values(
        '3.2', 
        'Do you list all FOSS components and their respective Identified Licenses from which Supplied Software is comprised?',
        'YES_NO','Yes', (select id from section where name='G3'));        
insert into question(number, question, type, correct_answer, section_id) values(
        '3.3', 
        'Is there a procedure for identifying and listing all FOSS components and their respective Identified Licenses) from which Supplied Software is comprised?',
        'YES_NO','Yes', (select id from section where name='G3'));           
insert into question(number, question, type, correct_answer, section_id) values(
        '3.4', 
        'Is this procedure documented?',
        'YES_NO','Yes', (select id from section where name='G3'));          
insert into question(number, question, type, correct_answer, section_id) values(
        '3.5', 
        'Do you archive the list of FOSS components and their respective Identified Licenses from which Supplied Software is comprised?',
        'YES_NO','Yes', (select id from section where name='G3'));      
insert into question(number, question, type, correct_answer, section_id) values(
        '3.6', 
        'Is there a procedure for archiving all FOSS components and their respective Identified Licenses from which Supplied Software is comprised?',
        'YES_NO','Yes', (select id from section where name='G3'));           
insert into question(number, question, type, correct_answer, section_id) values(
        '3.7', 
        'Is this procedure documented?',
        'YES_NO','Yes', (select id from section where name='G3'));          
insert into question(number, question, type, correct_answer, section_id) values(
        '3.8', 
        'Have you set up a FOSS program?',
        'YES_NO','Yes', (select id from section where name='G3'));      
insert into question(number, question, type, correct_answer, section_id) values(
        '3.9', 
        'Is this FOSS program capable of handling at least the following typical FOSS use cases encountered by Software Staff for Supplied Software?',
        'SUBQUESTIONS','6', (select id from section where name='G3'));              
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.1', 
        'Distribution in binary form.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));              
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.2', 
        'Distribution in source form.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));         
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.3', 
        'Integration with other FOSS such that it may trigger copyleft obligations.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));           
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.4', 
        'Contains modified FOSS.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));           
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.5', 
        'Contains FOSS or other software under an incompatible license interacting with other components within the Supplied Software.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));           
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '3.9.6', 
        'Contains FOSS with attribution requirements.',
        'YES_NO','Yes', (select id from section where name='G3'),(select id from question where number='3.9'));           
insert into question(number, question, type, correct_answer, section_id) values(
        '3.10', 
        'Are you addressing the typical FOSS use cases encountered by Software Staff for Supplied Software?',
        'YES_NO','Yes', (select id from section where name='G3'));                    
insert into question(number, question, type, correct_answer, section_id) values(
        '3.11', 
        'Have you implemented a process to address these typical FOSS use cases?',
        'YES_NO','Yes', (select id from section where name='G3'));                
        
 insert into section(name, title, spec_version) values(
'G4','Deliver FOSS Content Documentation and Artifacts', 
(select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '4.1', 
        'Is the Supplied Software accompanied by the following information:',
        'SUBQUESTIONS','7', (select id from section where name='G4'));          
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.1', 
        'copyright notices,',
        'YES_NO','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));          
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.2', 
        'copies of Identified Licenses',
        'YES_NO','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));          
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.3', 
        'modification notifications,',
        'YES_NO','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));           
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.4', 
        'attribution notices,',
        'YES_NO','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));           
  insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.5', 
        'prominent notices,',
        'YES_NO_NA','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));           
   insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.6', 
        'source code,',
        'YES_NO_NA','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));           
   insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '4.1.7', 
        'written offers?',
        'YES_NO_NA','Yes', (select id from section where name='G4'),(select id from question where number='4.1'));              
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.2', 
        'Do you ensure the above Distributed Compliance Artifacts are distributed with Supplied Software?',
        'YES_NO','Yes', (select id from section where name='G4'));         
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.3', 
        'Have you set up a process to ensure the above Distributed Compliance Artifacts are distributed with Supplied Software?',
        'YES_NO','Yes', (select id from section where name='G4'));             
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.4', 
        'Is this process documented?',
        'YES_NO','Yes', (select id from section where name='G4'));             
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.5', 
        'Is this process available to the Software Staff?',
        'YES_NO','Yes', (select id from section where name='G4'));            
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.6', 
        'Do you archive copies of the Distributed Compliance Artifacts of the Supplied Software (e.g., legal notices, source code, SPDX documents)?',
        'YES_NO','Yes', (select id from section where name='G4'));           
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.7', 
        'Can you easily retrieve the archived copies of the Distributed Compliance Artifacts of the Supplied Software (e.g., legal notices, source code, SPDX documents)?',
        'YES_NO','Yes', (select id from section where name='G4'));          
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.8', 
        'Is the archived planned to exist for at least as long as the Supplied Software is offered or as required by the Identified Licenses (whichever is longer)?',
        'YES_NO','Yes', (select id from section where name='G4'));            
  insert into question(number, question, type, correct_answer, section_id) values(
        '4.9', 
        'Are there any compliance artifacts publicly available?',
        'YES_NO_EVIDENCE','Any', (select id from section where name='G4'));         
        
        
        
 insert into section(name, title, spec_version) values(
'G5','Understand FOSS Community Engagement', 
(select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '5.1', 
        'Do you allow contributions of your employees to FOSS projects on behalf of the organization?',
        'YES_NO','Yes', (select id from section where name='G5'));  
insert into question(number, question, type, correct_answer, section_id) values(
        '5.2', 
        'Do your employees have to follow rules, when they contribute to FOSS projects on behalf of the organization?',
        'YES_NO','Yes', (select id from section where name='G5'));  
insert into question(number, question, type, correct_answer, section_id) values(
        '5.3', 
        'Are these rules captured in a written policy (“FOSS Contribution Policy”)?',
        'YES_NO','Yes', (select id from section where name='G5'));          
insert into question(number, question, type, correct_answer, section_id) values(
        '5.4', 
        'Is this FOSS Contribution Policy available to all your employees?',
        'YES_NO','Yes', (select id from section where name='G5'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '5.5', 
        'Is your Software Staff aware of the existence of the FOSS Contribution Policy?',
        'YES_NO','Yes', (select id from section where name='G5'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '5.6', 
        'Do you make your Software Staff aware of the FOSS Contribution Policy through:',
        'SUBQUESTIONS','1', (select id from section where name='G5'));   
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.6.1', 
        'Training,',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.6'));  
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.6.2', 
        'An internal wiki,',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.6'));          
insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.6.3', 
        'Another practical communication method?',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.6'));            
insert into question(number, question, type, correct_answer, section_id) values(
        '5.7', 
        'Does the FOSS Contribution Policy include cover the following considerations:',
        'SUBQUESTIONS','4', (select id from section where name='G5'));          
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.7.1', 
        'Legal approval for license considerations business rationale or approval,',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.7'));          
 insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.7.2', 
        'Technical review of code to be contributed,',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.7'));          
  insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.7.3', 
        'Community engagement and interaction,',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.7'));         
   insert into question(number, question, type, correct_answer, section_id, subquestion_of) values(
        '5.7.4', 
        'Adherence to project-specific contribution requirements?',
        'YES_NO','Yes', (select id from section where name='G5'),(select id from question where number='5.7'));        
        
        