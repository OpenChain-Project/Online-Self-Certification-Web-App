/**
 * Copyright (c) 2016 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
/**
 * Model for supporting Open Chain certification
 * There are 2 parts to the model, the static survey database itself and the
 * dynamic survey responses.
 * The top level object for the static survey is Survey
 * The dynamic model consists of a Responder (the organization filling out the survey) and
 * the SurveyResponse (the answers to the questions by the Responder)
 * @author Gary O'Neall
 *
 */
package org.openchain.certification.model;