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
package com.griddynamics.convergence.demo.dar.utils;

import java.util.ArrayList;
import java.util.List;

import com.griddynamics.convergence.demo.dar.service.Trade;
import com.griddynamics.convergence.demo.dar.service.TradeKey;
import com.griddynamics.convergence.demo.dar.service.TradeStorage;

public class TradeCopyUtil {
	
	public static final void copyTrades(TradeStorage from, TradeStorage to, int bookCount) {
		for(int i = 0; i != bookCount; ++i) {
			int bookId = i;
			
			List<Trade> trades = new ArrayList<Trade>();
			for(TradeKey key : from.listTrades(bookId)) {
				Trade trade = from.getTrade(key);
				trades.add(trade);
			}
			
			to.addTrades(trades);
			System.out.printf("Added %s rows to trades db\n", trades.size() + " (book " + i + " of " + bookCount + ")");
		}
	}
}
