package net.ssehub.kernel_haven.incremental.storage.modelstore;

import java.io.IOException;
import java.util.Collection;

public abstract class AbstractModelStorage<ResultType> {

	public abstract Collection<ResultType> getModel(String tag) throws IOException;

	public abstract void storeModelForTag(Collection<ResultType> modelElement, String tag) throws IOException;



}
