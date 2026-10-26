package com.chiptrack.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlayerStatus {
    ACTIVE,
    EXITED
}

@Serializable
data class Player(
    val id: String,
    val name: String,
    /** 累计拿取（入局 + 中途拿取） */
    val totalTaken: Int,
    /** 累计退还（中途或终局退还） */
    val totalReturned: Int = 0,
    val status: PlayerStatus = PlayerStatus.ACTIVE
) {
    /**
     * 积分 / 盈亏 = 累计归还 − 累计拿取。
     * 拿取记为负向、归还记为正向；例如入局拿取 1000 后显示 -1000。
     */
    val profit: Int get() = totalReturned - totalTaken

    fun withTake(amount: Int): Player {
        require(amount > 0) { "拿取积分必须大于 0" }
        return copy(totalTaken = totalTaken + amount)
    }

    fun withReturn(amount: Int): Player {
        require(amount >= 0) { "退还积分不能为负数" }
        if (amount == 0) return this
        return copy(totalReturned = totalReturned + amount)
    }

    fun exitTable(): Player {
        if (status == PlayerStatus.EXITED) return this
        return copy(status = PlayerStatus.EXITED)
    }
}
