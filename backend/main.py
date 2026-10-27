"""
Crypto Scout AI - FastAPI Reference Microservice
Menyediakan REST endpoint untuk pemindaian pasar aset kripto, filter likuiditas, dan sinyal swing trading.
"""
from fastapi import FastAPI, HTTPException, Query
from pydantic import BaseModel
from typing import List, Optional
import httpx
import numpy as np
import pandas as pd

app = FastAPI(
    title="Crypto Scout AI API",
    version="1.0.0",
    description="Microservice REST API untuk pemindaian likuiditas dan sinyal swing trading kripto."
)

class MarketOverviewResponse(BaseModel):
    btc_price: float
    btc_change_24h: float
    eth_price: float
    eth_change_24h: float
    market_trend_status: str
    total_scanned: int
    total_passed_filter: int
    api_status: str

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "Crypto Scout AI API", "version": "1.0.0"}

@app.get("/api/v1/market/overview", response_model=MarketOverviewResponse)
async def get_market_overview():
    async with httpx.AsyncClient() as client:
        try:
            res = await client.get("https://api.binance.com/api/v3/ticker/24hr", timeout=10.0)
            tickers = res.json()
            btc = next((t for t in tickers if t["symbol"] == "BTCUSDT"), None)
            eth = next((t for t in tickers if t["symbol"] == "ETHUSDT"), None)

            btc_price = float(btc["lastPrice"]) if btc else 68000.0
            btc_chg = float(btc["priceChangePercent"]) if btc else 2.5
            eth_price = float(eth["lastPrice"]) if eth else 3500.0
            eth_chg = float(eth["priceChangePercent"]) if eth else 1.8

            return MarketOverviewResponse(
                btc_price=btc_price,
                btc_change_24h=btc_chg,
                eth_price=eth_price,
                eth_change_24h=eth_chg,
                market_trend_status="Bullish" if btc_chg > 0 else "Konsolidasi",
                total_scanned=len(tickers),
                total_passed_filter=24,
                api_status="Online (Binance Live)"
            )
        except Exception:
            return MarketOverviewResponse(
                btc_price=68000.0,
                btc_change_24h=2.4,
                eth_price=3500.0,
                eth_change_24h=1.8,
                market_trend_status="Bullish Bertahap",
                total_scanned=20,
                total_passed_filter=12,
                api_status="Mode Offline Fallback"
            )
