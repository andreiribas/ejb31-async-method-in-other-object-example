package com.andreiribas.ejb;
/* 
The MIT License (MIT)

Copyright (c) 2013 Andrei Gonçalves Ribas <andrei.g.ribas@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
 * 
 */


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.andreiribas.ejb.AsyncCalculator;

/**
 * @author Andrei Gonçalves Ribas <andrei.g.ribas@gmail.com>
 * 
 */
public class AsyncTester {

	private static Logger LOGGER;

	private static EJBContainer ejbContainer;

	private static Context ctx;

	@BeforeClass
	public static void setUp() {

		LOGGER = Logger.getLogger(AsyncTester.class);

		Map<String, File> properties = new HashMap<String, File>();

		properties.put(EJBContainer.MODULES, new File("target/classes"));

		ejbContainer = EJBContainer.createEJBContainer(properties);

		ctx = ejbContainer.getContext();

	}

	@AfterClass
	public static void tearDown() {
		ejbContainer.close();
	}

	@Test
	public void testAsyncMethod() throws NamingException, InterruptedException, ExecutionException {

		AsyncCalculator asyncCalculationBean = (AsyncCalculator) ctx
				.lookup("java:global/classes/asyncCalculator");

		TestCase.assertNotNull(asyncCalculationBean);

		LOGGER.debug("Calling Async method on AsyncCalculator bean.");

		long startTime = System.currentTimeMillis();

		Future<Double> asyncCalcResult = asyncCalculationBean.calc();

		long finishTime = System.currentTimeMillis();

		long timeDifference = finishTime - startTime;

		LOGGER.debug(String.format(
				"Async method call took %s milliseconds to complete.",
				timeDifference));

		// Bean code will return instantly as this will be an async call. We set 1 second as time tolerance, but it will execute much faster than that.
		TestCase.assertTrue(timeDifference < 1000);

		Double calcResult = null;

		startTime = System.currentTimeMillis();
		
		calcResult = asyncCalcResult.get();
		
		finishTime = System.currentTimeMillis();

		timeDifference = finishTime - startTime;

		LOGGER.debug(String.format(
				"Future.get() method call took %s milliseconds to complete.",
				timeDifference));

		// Future.get() will execute in at least 10 seconds because of the Thread.sleep() in the async method. It will only return when it's over.
		TestCase.assertTrue(timeDifference > 10000);

		LOGGER.debug(String
				.format("Finished calling Future.get() method on future object, and the result is %s.",
						calcResult));

	}
	
}
