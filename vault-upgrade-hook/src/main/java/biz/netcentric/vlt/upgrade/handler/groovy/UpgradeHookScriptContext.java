package biz.netcentric.vlt.upgrade.handler.groovy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import be.orbinson.aem.groovy.console.api.context.ScriptContext;
import org.apache.sling.api.resource.ResourceResolver;


public class UpgradeHookScriptContext implements ScriptContext {

	private ResourceResolver resourceResolver;
	private String userId;
	private String script;
	private String data;
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	public UpgradeHookScriptContext setData(String data) {
		this.data = data;
		return this;
	}

	public UpgradeHookScriptContext setResourceResolver(ResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
		return this;
	}

	public UpgradeHookScriptContext setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public UpgradeHookScriptContext setScript(String script) {
		this.script = script;
		return this;
	}

	@Override
	public String getData() {
		return data;
	}

	@Override
	public ByteArrayOutputStream getOutputStream() {
		return outputStream ;
	}

	@Override
	public PrintStream getPrintStream() {
		return new PrintStream(getOutputStream());
	}

	@Override
	public ResourceResolver getResourceResolver() {
		return resourceResolver;
	}

	@Override
	public String getScript() {
		return script;
	}

	@Override
	public String getUserId() {
		return userId;
	}
}
