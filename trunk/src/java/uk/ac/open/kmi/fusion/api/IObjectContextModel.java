/* Copyright (c) 2012, Knowledge Media Institute
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the <organization> nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.open.kmi.fusion.api;

import java.util.List;
import java.util.Map;

import uk.ac.open.kmi.fusion.api.impl.ApplicationContext;

public interface IObjectContextModel {

	public abstract void prepare();

	public abstract ApplicationContext getApplicationContext();

	public abstract void setApplicationContext(
			ApplicationContext applicationContext);

	public abstract String serializeQuerySPARQLSource();

	public abstract String serializeQuerySPARQLTarget();

	public abstract List<IAttribute> getSourceAttributes();

	public abstract List<IAttribute> getTargetAttributes();

	public abstract List<String> getRestrictedTypesSource();

	public abstract List<String> getRestrictedTypesTarget();

	public abstract IAttribute getSourceAttributeByVarName(String varName);

	public abstract IAttribute getTargetAttributeByVarName(String varName);

	public abstract Map<String, IAttribute> getSourceAttributesByVarName();

	public abstract Map<String, IAttribute> getTargetAttributesByVarName();

	public abstract Map<String, IAttribute> getSourceAttributesByPath();

	public abstract Map<String, IAttribute> getTargetAttributesByPath();

	public abstract IAttribute getTargetAttributeByPath(String path);

	public abstract IAttribute getSourceAttributeByPath(String path);

	public abstract void addInstance(IObjectContextWrapper instance);

	public abstract IObjectContextWrapper getInstance(String uri);

	public abstract IObjectContextWrapper getInstance(int index);

	public abstract List<IObjectContextWrapper> getInstances();

}