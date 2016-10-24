/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.gss.test.factory.mock;

import org.apache.camel.Component;
import org.apache.camel.component.mock.MockComponent;
import com.redhat.gss.test.factory.CamelComponentFactory;

/**
 *
 * @author dhawkins
 */
public class MockComponentFactory implements CamelComponentFactory {

    @Override
    public Component createComponent() {
        return new MockComponent();
    }
    
}
