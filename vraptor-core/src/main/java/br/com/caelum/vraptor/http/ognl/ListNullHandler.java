/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor.http.ognl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import ognl.OgnlContext;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.vraptor2.Info;

/**
 * Capable of instantiating lists.
 * 
 * @author Guilherme Silveira
 */
public class ListNullHandler {

    Object instantiate(Map context, Object target, Object property, OgnlContext ctx) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        int position = (Integer) property;
        Object listHolder = ctx.getCurrentEvaluation().getPrevious().getSource();
        String listPropertyName = ctx.getCurrentEvaluation().getPrevious().getNode().toString();
        Method listSetter = ReflectionBasedNullHandler.findMethod(listHolder.getClass(), "set"
                + Info.capitalize(listPropertyName), target.getClass(), null);
        Type[] types = listSetter.getGenericParameterTypes();
        Type type = types[0];
        if (!(type instanceof ParameterizedType)) {
            // TODO better
            throw new IllegalArgumentException("Vraptor does not support non-generic collection at "
                    + listSetter.getName());
        }
        Class typeToInstantiate = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        Object instance = typeToInstantiate.getConstructor().newInstance();
        List list = (List) target;
        while (list.size() <= position) {
            list.add(null);
        }
        Container container = (Container) context.get(Container.class);
        EmptyElementsRemoval removal = container.instanceFor(EmptyElementsRemoval.class);
        removal.add(list);
        list.set(position, instance);
        return instance;
    }

}
