package in.succinct.defs.controller;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.model.Model;
import com.venky.swf.integration.IntegrationAdaptor;
import com.venky.swf.path.Path;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.documents.Document;
import in.succinct.defs.db.model.did.documents.Signature;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class VerificationMethodsController extends AbstractDirectoryController<VerificationMethod> {
    public VerificationMethodsController(Path path) {
        super(path);
    }
    
    @RequireLogin(value = false)
    public View challenge(String controllerKeyName){
        String keyDid = getPath().getTarget().replace("/challenge","");
        VerificationMethod verificationMethod = find(VerificationMethod.class,keyDid);
        verificationMethod.challenge();
        return IntegrationAdaptor.instance(VerificationMethod.class, getIntegrationAdaptor().getFormatClass()).createResponse(getPath(), verificationMethod);
    }
    
    @RequireLogin(value = false)
    public View verify(String controllerKeyName){
        //id => https://server/controllers/controller_id#keyName
        String keyDid = getPath().getTarget().replace("/verify","");
        
        
        VerificationMethod verificationMethod = find(VerificationMethod.class,keyDid);
        if (verificationMethod.isVerified()){
            throw new RuntimeException("Already Verified");
        }
        String response = getResponse(verificationMethod);
        verificationMethod.verify(response);
        return IntegrationAdaptor.instance(VerificationMethod.class, getIntegrationAdaptor().getFormatClass()).createResponse(getPath(), verificationMethod);
    }
    
    protected String getResponse(VerificationMethod verificationMethod) {
        try {
            PublicKeyType keyType = PublicKeyType.valueOf(verificationMethod.getType());
            if (keyType != PublicKeyType.Dns) {
                return StringUtil.read(getPath().getInputStream());
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
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (getReturnIntegrationAdaptor() == null) {
            return map;
        }
        addToIncludedModelFieldsMap(map, Subject.class, List.of("ID","MOD_COUNT","NAME"));
        addToIncludedModelFieldsMap(map, VerificationMethod.class, List.of( "NAME" ));
        
        return map;
    }
}
