package in.succinct.defs.controller;

import com.venky.core.collections.SequenceSet;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.integration.IntegrationAdaptor;
import com.venky.swf.path.Path;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.subject.Service;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class SubjectsController extends AbstractDirectoryController<Subject> {
    
    public SubjectsController(Path path) {
        super(path);
    }
    
    @RequireLogin(value = false)
    public View verify(String controllerKeyName){
        //id => https://server/controllers/controller_id#keyName
        String[] parts = controllerKeyName.split("#");
        String controllerName = parts[0] ;
        String keyName = parts[1];
        String controllerDid = did(controllerName);
        
        
        Subject Subject = find(Subject.class,controllerDid);
        if (Subject == null){
            throw new RuntimeException("Do not recognise Subject " + controllerName);
        }
        
        String keyDid = did(controllerKeyName);
        //controllers/succinct.in#dns
        
        VerificationMethod verificationMethod = find(VerificationMethod.class,keyDid);
        if (verificationMethod.isVerified()){
            throw new RuntimeException("Already Verified");
        }
        if (verificationMethod.getControllerId() != Subject.getId()){
            throw new RuntimeException("Key not registered for Subject");
        }
        String response = getResponse(verificationMethod);
        verificationMethod.verify(response);
        return IntegrationAdaptor.instance(VerificationMethod.class, getIntegrationAdaptor().getFormatClass()).createResponse(getPath(), verificationMethod);
    }
    
    protected String getResponse(VerificationMethod verificationMethod) {
        try {
            PublicKeyType keyType = PublicKeyType.valueOf(verificationMethod.getType());
            if (keyType != PublicKeyType.Dns) {
                return StringUtil.read(getPath().getRequest().getInputStream());
            }else {
                /*
                String hostName = verificationMethod.getName();
                String domainName = verificationMethod.getController().getName();
                
                 */
                String hostName = verificationMethod.getPublicKey();
                
                //String txtValue = verificationMethod.getChallenge();
                
                Hashtable<String, String> env = new Hashtable<>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                
                try {
                    javax.naming.directory.DirContext dirContext
                            = new javax.naming.directory.InitialDirContext(env);
                    javax.naming.directory.Attributes attrs
                            = dirContext.getAttributes(hostName, new String[]{"TXT"});
                    javax.naming.directory.Attribute attr
                            = attrs.get("TXT");
                    
                    String txtRecord = "";
                    
                    if (attr != null) {
                        txtRecord = attr.get().toString();
                    }
                    if (ObjectUtil.isVoid(txtRecord)){
                        throw new RuntimeException("Please verify after some time. Dns propagation may take a while..");
                    }
                    
                    return txtRecord;
                } catch (javax.naming.NamingException e) {
                    throw new RuntimeException("Verification failed");
                }
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    

    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map =  super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        
        addToIncludedModelFieldsMap(map, VerificationMethod.class, List.of("CONTROLLER_ID" ));
        addToIncludedModelFieldsMap(map, Document.class, List.of("SUBJECT_ID" ));
        addToIncludedModelFieldsMap(map, Signature.class, List.of("DOCUMENT_ID" ));
        addToIncludedModelFieldsMap(map, Service.class, List.of("SUBJECT_ID" ));
        
        return map;
        
    }
}
