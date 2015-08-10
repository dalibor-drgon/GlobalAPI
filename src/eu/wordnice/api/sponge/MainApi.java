/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.api.sponge;

import java.util.Set;

import eu.wordnice.api.Api;

@org.spongepowered.api.plugin.Plugin(id="MainAPI", name="MainAPI", version="1.8.0")
public class MainApi {
	
	@com.google.inject.Inject
	private org.slf4j.Logger log;

	@org.spongepowered.api.event.Subscribe
    public void onServerStart(org.spongepowered.api.event.state.ServerStartedEvent event) {
		try {
			Set<String> clzs = Api.getClasses(Api.getClassesLocation(org.spongepowered.api.Server.class));
			this.log.info("Sponge classes: " + clzs.size());
			this.log.info("Sponge packages: " + Api.filterPackagesString(clzs, null).size());
		} catch(Throwable t) {}
		
		this.log.info("MainAPI by wordnice for Sponge was enabled! Hello!");
	}
	
	@org.spongepowered.api.event.Subscribe
    public void onServerStart(org.spongepowered.api.event.state.ServerStoppingEvent event) {
		this.log.info("MainAPI by wordnice was disabled! Bye!");
	}
	
}
