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
package com.griddynamics.convergence.demo.dar.service.stub;

import java.io.Serializable;

import com.griddynamics.convergence.demo.dar.service.TradeKey;

public class TradeKeyObject implements TradeKey, Serializable {

	private static final long serialVersionUID = 1L;

	private String portfolioId;
	private int bookId;
	private int tradeId;

	public TradeKeyObject(TradeKey key) {
		this(key.getPortfolioId(), key.getBookId(), key.getTradeId());		
	}
	
	public TradeKeyObject(String portfolioId, int bookId, int tradeId) {
		this.bookId = bookId;
		this.portfolioId = portfolioId;
		this.tradeId = tradeId;
	}

	public int getBookId() {
		return bookId;
	}

	public String getPortfolioId() {
		return portfolioId;
	}

	public int getTradeId() {
		return tradeId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bookId;
		result = prime * result
				+ ((portfolioId == null) ? 0 : portfolioId.hashCode());
		result = prime * result + tradeId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradeKeyObject other = (TradeKeyObject) obj;
		if (bookId != other.bookId)
			return false;
		if (portfolioId == null) {
			if (other.portfolioId != null)
				return false;
		} else if (!portfolioId.equals(other.portfolioId))
			return false;
		if (tradeId != other.tradeId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + portfolioId + ", " + bookId + ", " + tradeId + ")";
	}
}
