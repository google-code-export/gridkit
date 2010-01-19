/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.convergence.demo.dar.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.griddynamics.convergence.demo.dar.service.stub.TradeKeyObject;
import com.griddynamics.convergence.demo.dar.service.stub.TradeObject;

public class RandomTradeStorage implements TradeStorage {
	
	private int garbageSize = 20;
	private int tradesPerBook = 10000;
	private Random rnd  = new Random();
	
	public RandomTradeStorage() {
	}
	
	public RandomTradeStorage(int garbageSize, int tradesPerBook) {
		this.garbageSize = garbageSize;
		this.tradesPerBook = tradesPerBook;
	}

	public void setGarbageSize(int garbageSize) {
		this.garbageSize = garbageSize;
	}

	public void setTradesPerBook(int tradesPerBook) {
		this.tradesPerBook = tradesPerBook;
	}

	public void addTrades(Collection<Trade> trades) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();		
	}

	public Trade createTrade(String portfolioId, int bookId, int tradeId) {
		TradeKeyObject key = new TradeKeyObject(portfolioId, bookId, tradeId);
		Trade trade = new TradeObject(key);
		return trade;
	}

	public String getBookLocation(int bookId) {
		return null;
	}
	
	@Override
	public String getTradeLocation(TradeKey key) {
		return null;
	}

	public synchronized Trade getTrade(TradeKey key) {
		initRnd(key);
		TradeObject trade = new TradeObject(new TradeKeyObject(key));
		trade.price = rnd.nextGaussian() * 10 + 1000;
		trade.xmlData = genData();
		
		return trade;
	}

	private void initRnd(TradeKey key) {
		int seed = key.hashCode();
		rnd.setSeed(seed);
	}

	private byte[] genData() {
		int size = (int)(rnd.nextGaussian() * 5 + garbageSize);
		byte[] data = new byte[size > 0 ? size : 1];
		for(int i = 0; i != data.length; ++i) {
			data[i] = (byte) ('A' + rnd.nextInt(23));
		}
		return data;
	}

	public Collection<TradeKey> listTrades(int bookId) {
		TradeKey[] keys = new TradeKey[tradesPerBook];
		for(int i = 0; i != tradesPerBook; ++i) {
			keys[i] = new TradeKeyObject("portfolio-x", bookId, i << 16 | bookId);
		}
		return Arrays.asList(keys);
	}
	
	public static class Provider implements TradeStorageProvider {
		private static final long serialVersionUID = 20090811L;

		private int garbageSize = 20;
		private int tradesPerBook = 10000;
		
		public Provider() {
		}
		
		public Provider(int garbageSize, int tradesPerBook) {
			this.garbageSize = garbageSize;
			this.tradesPerBook = tradesPerBook;
		}

		public TradeStorage getStorage() {
			return new RandomTradeStorage(garbageSize, tradesPerBook);
		}
	}
}
