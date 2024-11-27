package in.succinct.defs.controller;

import com.venky.core.collections.SequenceSet;
import com.venky.core.security.Crypt;
import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.application.ApplicationUtil;
import com.venky.swf.db.model.io.ModelIOFactory;
import com.venky.swf.db.model.io.ModelReader;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.integration.api.HttpMethod;
import com.venky.swf.path.Path;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.defs.db.model.did.subject.Subject;
import in.succinct.defs.db.model.did.subject.VerificationMethod;
import in.succinct.defs.db.model.did.subject.VerificationMethod.PublicKeyType;
import in.succinct.defs.db.model.did.subject.VerificationMethod.Purpose;
import in.succinct.defs.util.SignatureChallenge;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractDirectoryController<M extends Model & Did> extends ModelController<M> {
    
    @Override
    @RequireLogin(false)
    public View erd() {
        return super.erd();
    }
    
    
    public static Path ensureDefaultHeaders(Path path){
        path.getHeaders().put("Content-Type","application/json");
        return path;
    }
    protected AbstractDirectoryController(Path path) {
        super(ensureDefaultHeaders(path));
    }
    protected M find(String name){
        return find(getModelClass(),did(name));
    }
    protected  String did(String name){
        return String.format("%s",
                getPath().getTarget()); // Target includes name
    }
    protected <D extends Model & Did> D find(Class<D> clazz, String did){
        D aDid = Database.getTable(clazz).newRecord();
        aDid.setDid(did);
        return Database.getTable(clazz).getRefreshed(aDid);
    }
    protected <D extends Model & Did> List<D> findAll(Class<D> clazz, String did){
        Select select = new Select().from(getModelClass());
        select.where(new Expression(select.getPool(),"DID", Operator.LK,
                "%s%%".formatted(did)));
        return select.execute();
    }
    
    
    @RequireLogin(false)
    @SuppressWarnings("unchecked")
    public View index(String name){
        loadSigner();
        
        List<M> dids = findAll(getModelClass(),did(name));
        
        HttpMethod method = HttpMethod.valueOf(getPath().getRequest().getMethod());
        switch (method){
            case POST,PUT -> {
                try {
                    if (dids.size() == 1){
                        InputStream is = getPath().getInputStream();
                        JSONObject object = JSONObjectWrapper.parse(is);
                        object.put("Did",did(name));
                        ModelReader<M,JSONObject> reader = ModelIOFactory.getReader(getModelClass(),JSONObject.class);
                        M aDid = reader.read(object,true);
                        return respond(aDid);
                    }else {
                        throw new RuntimeException("Incomplete did " + name);
                    }
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            case GET -> {
                return respond(dids);
            }
            case DELETE -> {
                for (M aDid : dids){
                    aDid.destroy();
                }
                return getIntegrationAdaptor().createStatusResponse(getPath(),null);
            }
            default ->  {
                throw new RuntimeException("Unknown Http method " + method);
            }
        }
        
        
    }
    
    @RequireLogin(false)
    public View index(){
        loadSigner();
        HttpMethod method = HttpMethod.valueOf(getPath().getRequest().getMethod());
        switch (method){
            case POST,PUT -> {
                List<M> dids = getIntegrationAdaptor().readRequest(getPath(),true);
                return respond(dids);
            }
            case GET -> {
                Select select = new Select().from(getModelClass());
                select.where(new Expression(select.getPool(),"DID", Operator.LK,
                        "%s%%".formatted(getPath().getTarget())));
                return respond(select.execute());
            }
            case DELETE -> {
                for (M did: new Select().from(getModelClass()).execute(getModelClass())) {
                    did.destroy();
                }
                return getIntegrationAdaptor().createStatusResponse(getPath(),null);
            }
            default ->  {
                throw new RuntimeException("Unknown Http method " + method);
            }
        }
        
    }
    
    private View respond(M model){
        return getIntegrationAdaptor().
                createResponse(getPath(),model,
                getIncludedFields() == null ? null : Arrays.asList(getIncludedFields()),
                getIgnoredParentModels(),  getIncludedModelFields());
    }
    public View respond(List<M> models){
        return getIntegrationAdaptor().
                createResponse(getPath(),models,
                        getIncludedFields() == null ? null : Arrays.asList(getIncludedFields()),
                        getIgnoredParentModels(), getConsideredChildModels(), getIncludedModelFields());
        
    }
    
    public List<String> getExistingModelFields(Map<Class<? extends Model>,List<String>> map , Class<? extends Model> modelClass){
        List<String> existing = new SequenceSet<>();
        if (map != null) {
            ModelReflector.instance(modelClass).getModelClasses().forEach(mc -> {
                List<String> e = map.get(mc);
                if (e != null) {
                    existing.addAll(e);
                }
            });
        }
        return existing;
    }
    protected void addToIncludedModelFieldsMap(Map<Class<? extends Model>,List<String>> map, Class<? extends Model> clazz , List<String> excludedFields){
        List<String> fields = ModelReflector.instance(clazz).getVisibleFields(List.of("ID"));
        List<String> oldFields = getExistingModelFields(map,clazz);
        
        Map<Class<? extends Model>,List<String>> requestedFieldsMap  = getIncludedModelFieldsFromRequest();
        List<String> requestedFields = getExistingModelFields(requestedFieldsMap,clazz);
        
        
        SequenceSet<String> finalFields = new SequenceSet<>();
        finalFields.addAll(fields);
        finalFields.addAll(oldFields);
        excludedFields.forEach(finalFields::remove);
        finalFields.addAll(requestedFields);// Ensure Requested is always added
        map.put(clazz,finalFields);
    }
    
    public void loadSigner() {
        String authorization = StringUtil.valueOf(getPath().getHeader("Authorization"));
        Map<String,String> params = ApplicationUtil.extractAuthorizationParams(authorization);
        if (params.isEmpty()){
            return;
        }
        
        InputStream payload = null;
        try {
            payload = getPath().getInputStream();
        }catch (IOException exception){
            throw new RuntimeException(exception);
        }
        String keyDid = params.get("keyId");
        
        VerificationMethod verificationMethod = find(VerificationMethod.class,keyDid);
        if (VerificationMethod.Purpose.valueOf(verificationMethod.getPurpose()) != Purpose.Assertion
                || !verificationMethod.isVerified()
                || VerificationMethod.PublicKeyType.valueOf(verificationMethod.getType()) !=  PublicKeyType.Ed25519 ){
            throw new AccessDeniedException("Only Ed25519 Assertion keys that are verified may be used for signing");
        }
        Subject controller = verificationMethod.getController();
        
        
        if (!ObjectUtil.isVoid(verificationMethod.getHashingAlgorithm())) {
            String digest = Crypt.getInstance().toBase64(Crypt.getInstance().digest(VerificationMethod.HashAlgorithm.valueOf(verificationMethod.getHashingAlgorithm()).algo(), StringUtil.read(payload)));
            if (!params.containsKey("digest")) {
                params.put("digest", String.format("%s=%s",VerificationMethod.HashAlgorithm.valueOf(verificationMethod.getHashingAlgorithm()).commonName() , digest));
            }
        }
        if (!params.containsKey("request-target")){
            params.put("request-target",getPath().getRequest().getMethod().toLowerCase() + " " + getPath().getTarget());
        }
        if (!params.containsKey("headers")) {
            params.put("headers", "(request-target) (created) digest");
        }
        
        String signingString = ApplicationUtil.getSigningString(params);
        Config.instance().getLogger(getClass().getName()).info("Signing String|"+ signingString +"|Length:%d-------------".formatted(signingString.length()));
        if (!SignatureChallenge.getInstance().verify(verificationMethod,signingString,params.get("signature"))){
            throw new AccessDeniedException("Signature mismatched");
        }
        
        Database.getInstance().setContext(Subject.class.getName(),controller);
    }
    
}
