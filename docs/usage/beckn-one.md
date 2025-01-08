# Beckn-One
## TL;DR
Beckn-One is an ambitious community project by Beckn Open Collective that aims to create a single global decentralized transaction network based on open interoperable protocols as opposed to multiple semi-siloed beckn-enabled networks that exist today. 

All parties that need to be part of a  commercial transaction, can make use of these network protocols and collaborate digitally. 
This network has taken inspirations form several protocol based open networks we already use. For e.g. internet, mail, ftp, telephony.

## Terminology
> TODO: Need a Terminology Section

## Context 
Open networks like ONDC, ONEST, UEI, and UHI, etc although federated, operate as a closed system managing platform authentication and onboarding. This is done via a governing body or organization called the **Network Facilitator Organization (NFO)** that determines “who” gets to participate and who doesn’t. This is done via various onboarding processes that differ across regions, domains. Such processes lead to inefficiencies and potential delays in adoption. These processes are not intentional but emerge as a result of several factors like trust deficiencies between platforms, lack of awareness domain and region-specific policies, legacy platform mindsets, etc. 

## Problem Articulation
Beckn protocol is already a peer-to-peer protocol for transactions. The issue isn't transactions but in establishment of trust between the transacting parties before and during a transaction. Let's understand this using a simple use case. 

Adam lives in Bangalore, India and wants to buy 1000 drones for his company XFlight. Lisa is a seller in Shenzen, China who owns a drone manufacturing company - Quad Engineering. Adam and Lisa don't know about each other. Adam uses Open Commerce, a Beckn-enabled app which is registered on the ONDC registry. Lisa has published her catalog on Acme, another Beckn-enabled seller platform on Open Schenzen Network. Both ONDC and Open Schenzen Network have onboarded Open Commerce and Acme on their registries using their respective onboarding process.

Adam, opens the Open Commerce app and searches for Drones. He sees a small list of drone products that are being sold by some importers. Adam realizes that not too many sellers on ONDC sell drones. Furthermore, Lisa's company - Quad Engineering doesn't even show up on Adam's app - Open Commerce, during search; simply because, Acme, (Lisa's app) is not 
_registered_ on the ONDC registry. Similarly, Adam's platform, Open Commerce isn't registered on the Open Schenzen Network registry.

Adam discovers Quad Engineering using a Google Search and opens their website. Upon opening the website, he sees an old-school website that doesn't instill any trust on the manufacturer - Lisa. He sees very few reviews on Google, since Google has limited operations in China. He calls up Quad Engineering and has a conversation with a sales executive. Due to a language barrier, he isn't able to trust the executive. The same happens on the other side. The sales executive doesn't know whether to trust Adam. She mentions that a payment of 40,000 USD must be made upfront to place the order. Adam is worried that he may not receive the shipment as he doesn't trust the company. 

Frustrated, 

How do we enable creation of a single global network of Beckn Protocol nodes  (like the internet) that allow any two entities to discover and transact with each other in a trusted manner without the need of complicated onboarding processes and centralized trust authorities? 

## Forces
> TODO: Forces in the problem that may constrain the solution

## Solution


### Actors
#### Registrars. 
Registrars are a crucial entity in the beckn one ecosystem. They are entities who provide [[#Registration Services]] to entities (individuals and companies) who would like to participate in commercial transactions over beckn-one. These are similar to a network provider in the telephony network.

#### Registries
These are platforms that are compliant with the [Registry specifications](https://github.com/beckn/protocol-specifications/blob/master/api/registry/build/registry.yaml)
Registrars who  provide these services, can get their platforms additionally certified by  Beckn Certification Agencies who can test the platform for compliance and issue a certificate of compliance. Automated tests may be published by the Beckn-one ecosystem volunteers from time to time. Registrars can run these tests and self-certify their platform's compliance. External certification agencies (testing agencies) may also run these tests and certify a registration service.

## Functional Requirements
1. Any entity should be able to  self generate a Decentralized Identifier (DiD for short) which can be registered with any of the registrars (list maintained on Beckn GitHub)
2. Entities can register their DiD on one of the compliant registrars. 
	1. The following artifact(s) would be mandatory during registration.
		1. A Cryptographic Public Signing Key (Ed25519), that would be used to verify the ownership of the DiD.
3. Entities can publish/update the following meta information against their DiD
	1. More Cryptographic Keys for various puposes
		1. X25519 for encryption.
		2. Ed25519 for Assertion, Authentication..
	2. Email  For communication
	3. Phone for communication
	4. Domain name to host services. 
*This meta information can be verified by the registrar by any of the prescribed method in the specification. (e.g phone and email by otp , domain name with a DNS TXT record, Signing keys with a signing challenge and Encryption keys with an encryption challenge )*

4. Entities can publish self signed documents  about themselves with their registrar 
5. Entities can publish signed documents of _other_ entities, registered with other registrars with their registrar. (Issuing a credential)
7. Entities registered with one registrar can sign any document hosted in another registry with their personal Signing Key. ( This is useful in contract signing )
8. Entities can publish, services hosted by them on their verified domains. Service specification documents can be either published by self or could be one of the specifications published by Others (like FIDE or similar organizations)

## Registry Apis to query

### Subject  URL
https://registry_service_base_url/subjects/:subject_id
Usually subject Id is either a domain name owned by a subject, a phone number , email or some random uuid. 
If a subject moves across registry service, it can port its /subjects/:subject_id from https://registry_service_base_url/ to https://another_registry_service_base_url/

### A Subject's verification methods
https://registry_service_base_url/subjects/:subject_id/verification_methods

### A Specific verification method details
https://registry_service_base_url/subjects/:subject_id/verification_methods/:method_id

### Documents published on a subject
https://registry_service_base_url/subjects/:subject_id/documents

### A Document detail
https://registry_service_base_url/subjects/:subject_id/documents/:document_id
*A Document subject could be on another registry*

### Services published 

https://registry_service_base_url/subjects/:subject_id/services


### Detail of a specific service 
https://registry_service_base_url/subjects/:subject_id/services/:service_id

*A service may point to a specification document in another registry.*
	

## Directory Protocol a closely related protocol to registry protocol

This is a meta specification for any application to publish its data. 
It standardizes method to publish data url and the corresponding payload for an application end point. 

1. Any web site can put a directory.txt at a well known location e.g https://example.com/.dedi/directory.txt
2. directory.txt contains the directory server base_url for the domain
	1. e.g https://directory.example.com/ or a general https://xyz.com/
3. The directory server is expected to publish the data according to the decentralized directory protocol

for e.g 
1.  https://a_directory_server/ 
Lists a json array of directory names it serves. usually a small set 
e.g ["companies","users","domains"]

2. https://a_directory_server/:directory/schema 
	1. A jsonschema for the directory. It describes schema for contents that would be returned by the api https://a_directory_server/:directory/:record_id
4. https://a_directory_server/:directory/:record_id (To query a specific record)
return json data according to the specification of /:directory/schema 

	e.g https://a_directory_server/domains/humbhionline.in

*Note: directory names are likely to be controlled and permissioned by specific registrars allowed to publish that directory. For.e.g /companies/ may be assigned  to mca.gov.in , /domains may be allowed only for domain registrars and so on.
Also the schema could be evolved for these directories in collaboration with other directory registrars for the same name.

# An Example of setting up a virtual network of ecommerce participants (e.g ondc.org)
1. A Network faciliator e.g ondc.org hosts a file directory.txt under ondc.org/.dedi/ 
2. https://ondc.org/.dedi/directory.txt points to directory.ondc.org 
3. https://directory.ondc.org/ lists
	1. ["registrars","subscribers","usecases","agreements","domains", "policies"]
4. https://directory.ondc.org/registrars
	1. return list registrars who manage ondc's  dids . 
		1. e.g ["facilitator-one.becknprotocol.io"] 
		2. https://directory.ondc.org/registrars/schema 
			1. { "did_url" : "" }
		3. https://directory.ondc.org/registrars/facilitator-one.becknprotocol.io
			1. {"did_url" : "https://facilitator-one.becknprotocol.io/subjects/ondc.org" }
	2. Ondc stores its documents, verification methods/keys etc on the the registrar facilicator-one.becknprotocol.io.
5. https://directory.ondc.org/subscribers/schema
		e.g 
```
			{
				"registrar" :"subscriber's registrar",
				"did_path" :"/subjects/:subscriber_id" , 
				"agreements_signed" : ["did_path_of_agreement_document in ondc.org's registrar  additionally signed by subscriber's private key"], 
				"supported_usecases" : [ did_path to supported usecase document signed by a Subscriber and/or by a CA]
				
			}
			
			A supported usecase document  Has the following structure 
			{
				"service_id" : "did of a service endpoint of the subject/subscriber_id"
				"domain" :"" 
				"usecase_id" : "did to a published usecase document"
			}
```
		
1. https://directory.ondc.org/subscribers/:subscriber_id
	1. returns a json document according to the schema above.
	2. Note, different parts of the json output are signed by different parties.



## A Reference implementation of DID 
Repo: https://github.com/beckn-on-succinct/defs
Hosted at : https://defs.humbhionline.in
Documentation: https://defs.humbhionline.in/html/index.html

	









