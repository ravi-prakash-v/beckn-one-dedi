# Decentralized File system (a succinct dedi project) 

## The problem
Entities that transact with each other often need to know details about their counter parties, learning about the credentials they posses, their reputations, etc... 

These information are typically available in different systems, owned by government, non governmental organizations,social media or private organzations. 


### DeDi 
Decentralized directory (DeDi) is a protocol ideated within the FIDE/Finternet ecosystem. The purpose was to define a consistent way to get information from systems that processes the data. It is not necessarily related to data of transacting entities, It is a protocol  to access catagorized information from within any application. 
e.g 
/dns/some_domain_name (could be made available by domain registrars)

/IND/tax/tax_id could be made available by tax authorities in india
/USA/tax/tax_id could be made available by tax authorities in USA ...


This approach is expected to reduce integration costs across the Digital economy. 

## What is Did protocol and how is it releated?

The Decentralized Identifiers (DIDs) defined in [this specification](https://www.w3.org/TR/did-core) are a new type of globally unique identifier. They are designed to enable individuals and organizations to generate their own identifiers using systems they trust. These new identifiers enable entities to prove control over them by authenticating using cryptographic proofs such as digital signatures. 


## What is DeFs
Decentralized filesystem is an generic implementation of DeDi protocol that provides information for entities identified by DIds


### Concepts from DId

1. Subject

Any entity (a person, organization, thing, data model, abstract entity, etc) that is described  by a controller.

2. Controller 

Any entity (...) that controls or handles another subject's information. 
e.g a secretary handing instagram account of a celebrity. 



3. VerificationMethod

Depending on the purpose (Authentication/Assertion.. ) a Controller can verify themselves using one of the verification methods(e.g crypto keys, otp,..) designated for the purpose 

4. Service

Services offered by a subject 

5. Documents

A Document that describes some thing about a subject

for e.g Pan card is a document issued to a *company/individual* by india's tax authorities. 

6. Signatures. 

Cryptographic signatures are used to update subjects, issue documents, sign contracts ... 

These signed updates can be verified by any one else and thus trusted if the signer is a trusted entity. 



# Some implementation Details

* Onboarding a controller. 
    *   Adding a subject where controller is blank 
        /subjects (POST)

    *   Adding a verification method.
        /subjects (POST)  to add a subject with one or more verification methods
        /subjects/:subject_id (POST)  to add one or more verification methods
        /subjects/:subject_id/verification_methods (POST) to add one or more verification methods

    *   Verifying the method with a challenge. 
        /subjects/:subject_id/verification_methods/verify/:verification_id POST with payload containing response to a challenge.
            * This could be a signature 
            * Decrypted value an encrypted challenge

    *   Using verified methods to update itself. 
        * Add Documents
        * Add Services.
        * Add more verification methods


* Onboarding a subject. 
    *   A Controller can add a subject using its verified method and become the controller for this new subject.
        /subjects (POST)








