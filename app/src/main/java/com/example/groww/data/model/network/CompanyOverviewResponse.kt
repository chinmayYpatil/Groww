package com.example.groww.data.model.network


import com.google.gson.annotations.SerializedName

data class CompanyOverviewResponse(
    @SerializedName("Symbol")
    val symbol: String,
    @SerializedName("AssetType")
    val assetType: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("CIK")
    val cik: String,
    @SerializedName("Exchange")
    val exchange: String,
    @SerializedName("Currency")
    val currency: String,
    @SerializedName("Country")
    val country: String,
    @SerializedName("Sector")
    val sector: String,
    @SerializedName("Industry")
    val industry: String,
    @SerializedName("Address")
    val address: String,
    @SerializedName("FiscalYearEnd")
    val fiscalYearEnd: String,
    @SerializedName("LatestQuarter")
    val latestQuarter: String,
    @SerializedName("MarketCapitalization")
    val marketCapitalization: String,
    @SerializedName("EBITDA")
    val ebitda: String,
    @SerializedName("PERatio")
    val peRatio: String,
    @SerializedName("PEGRatio")
    val pegRatio: String,
    @SerializedName("BookValue")
    val bookValue: String,
    @SerializedName("DividendPerShare")
    val dividendPerShare: String,
    @SerializedName("DividendYield")
    val dividendYield: String,
    @SerializedName("EPS")
    val eps: String,
    @SerializedName("RevenuePerShareTTM")
    val revenuePerShareTTM: String,
    @SerializedName("ProfitMargin")
    val profitMargin: String,
    @SerializedName("OperatingMarginTTM")
    val operatingMarginTTM: String,
    @SerializedName("ReturnOnAssetsTTM")
    val returnOnAssetsTTM: String,
    @SerializedName("ReturnOnEquityTTM")
    val returnOnEquityTTM: String,
    @SerializedName("RevenueTTM")
    val revenueTTM: String,
    @SerializedName("GrossProfitTTM")
    val grossProfitTTM: String,
    @SerializedName("DilutedEPSTTM")
    val dilutedEPSTTM: String,
    @SerializedName("QuarterlyEarningsGrowthYOY")
    val quarterlyEarningsGrowthYOY: String,
    @SerializedName("QuarterlyRevenueGrowthYOY")
    val quarterlyRevenueGrowthYOY: String,
    @SerializedName("AnalystTargetPrice")
    val analystTargetPrice: String,
    @SerializedName("TrailingPE")
    val trailingPE: String,
    @SerializedName("ForwardPE")
    val forwardPE: String,
    @SerializedName("PriceToSalesRatioTTM")
    val priceToSalesRatioTTM: String,
    @SerializedName("PriceToBookRatio")
    val priceToBookRatio: String,
    @SerializedName("EVToRevenue")
    val evToRevenue: String,
    @SerializedName("EVToEBITDA")
    val evToEbitda: String,
    @SerializedName("Beta")
    val beta: String,
    @SerializedName("52WeekHigh")
    val fiftyTwoWeekHigh: String,
    @SerializedName("52WeekLow")
    val fiftyTwoWeekLow: String,
    @SerializedName("50DayMovingAverage")
    val fiftyDayMovingAverage: String,
    @SerializedName("200DayMovingAverage")
    val twoHundredDayMovingAverage: String,
    @SerializedName("SharesOutstanding")
    val sharesOutstanding: String,
    @SerializedName("SharesFloat")
    val sharesFloat: String,
    @SerializedName("PercentInsiders")
    val percentInsiders: String,
    @SerializedName("PercentInstitutions")
    val percentInstitutions: String,
    @SerializedName("DividendDate")
    val dividendDate: String,
    @SerializedName("ExDividendDate")
    val exDividendDate: String
)