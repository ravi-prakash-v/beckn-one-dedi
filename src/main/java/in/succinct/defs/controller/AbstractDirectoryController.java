package in.succinct.defs.controller;

import com.venky.core.collections.SequenceSet;
import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.io.ModelIOFactory;
import com.venky.swf.db.model.io.ModelReader;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.integration.api.HttpMethod;
import com.venky.swf.path.Path;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Select;
import com.venky.swf.views.View;
import in.succinct.defs.db.model.did.identifier.Did;
import in.succinct.json.JSONObjectWrapper;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractDirectoryController<M extends Model & Did> extends ModelController<M> {
    protected AbstractDirectoryController(Path path) {
        super(path);
    }
    protected M find(String name){
        return find(getModelClass(),did(name));
    }
    protected  String did(String name){
        return String.format("%s/%s",
                getPath().controllerPathElement(),name);
    }
    public String didUrl(String name){
        return String.format("%s/%s/%s",
                Config.instance().getServerBaseUrl(), getPath().controllerPathElement(),name);
    }
    protected <D extends Model & Did> D find(Class<D> clazz, String did){
        D aDid = Database.getTable(clazz).newRecord();
        aDid.setDid(did);
        return Database.getTable(clazz).getRefreshed(aDid);
    }
    
    @RequireLogin(false)
    @SuppressWarnings("unchecked")
    public View index(String name){
        
        M aDid = find(name);
        if (aDid.getRawRecord().isNewRecord()){
            throw new RuntimeException("Cannot identify did " + aDid.getDid());
        }
        
        HttpMethod method = HttpMethod.valueOf(getPath().getRequest().getMethod());
        switch (method){
            case POST,PUT -> {
                try {
                    InputStream is = getPath().getInputStream();
                    JSONObject object = JSONObjectWrapper.parse(is);
                    object.put("Did",did(name));
                    ModelReader<M,JSONObject> reader = ModelIOFactory.getReader(getModelClass(),JSONObject.class);
                    aDid = reader.read(object,true);
                    return respond(aDid);
                }catch (Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            case GET -> {
                return respond(aDid);
            }
            case DELETE -> {
                aDid.destroy();
                return getIntegrationAdaptor().createStatusResponse(getPath(),null);
            }
            default ->  {
                throw new RuntimeException("Unknown Http method " + method);
            }
        }
        
        
    }
    
    @RequireLogin(false)
    public View index(){
        HttpMethod method = HttpMethod.valueOf(getPath().getRequest().getMethod());
        switch (method){
            case POST,PUT -> {
                List<M> dids = getIntegrationAdaptor().readRequest(getPath(),true);
                return respond(dids);
            }
            case GET -> {
                return super.index();
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
    
}
