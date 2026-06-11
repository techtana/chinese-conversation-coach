package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.ProficiencyLevel
import com.mandarincoach.app.data.model.Scenario
import com.mandarincoach.app.data.model.ScenarioItem

object ScenarioRepository {

    val scenarios: List<Scenario> = listOf(
        Scenario(
            id = "hotel_checkin",
            title = "Hotel Check-In",
            hanziTitle = "酒店入住",
            emoji = "🏨",
            description = "Check into your hotel after a long flight",
            aiRole = "a hotel receptionist at the front desk",
            userRole = "a tired traveler checking in",
            setting = "The lobby of a busy hotel in Beijing, evening",
            goals = listOf(
                "Greet the receptionist and say you have a reservation",
                "Confirm your name and number of nights",
                "Ask for the Wi-Fi password",
                "Get your room key"
            ),
            items = listOf(
                ScenarioItem("room_key", "Room Key", "房卡", "🔑"),
                ScenarioItem("wifi_password", "Wi-Fi Password", "无线网密码", "📶")
            ),
            minLevel = ProficiencyLevel.BEGINNER,
            stampTitle = "Hotel Check-In Certified"
        ),
        Scenario(
            id = "ordering_food",
            title = "Ordering Dinner",
            hanziTitle = "点菜",
            emoji = "🍜",
            description = "Order a meal at a local restaurant",
            aiRole = "a waiter at a busy noodle restaurant",
            userRole = "a hungry customer",
            setting = "A small, popular noodle restaurant at dinner time",
            goals = listOf(
                "Ask for the menu",
                "Ask about a dish (spicy? what's in it?)",
                "Order food and a drink",
                "Receive the right dish"
            ),
            items = listOf(
                ScenarioItem("menu", "Menu", "菜单", "📋"),
                ScenarioItem("the_right_dish", "The Right Dish", "你点的菜", "🍲")
            ),
            minLevel = ProficiencyLevel.BEGINNER,
            stampTitle = "Restaurant Survivor"
        ),
        Scenario(
            id = "taxi",
            title = "Taxi Ride",
            hanziTitle = "打车",
            emoji = "🚕",
            description = "Get a taxi across town to your meeting",
            aiRole = "a chatty taxi driver",
            userRole = "a passenger in a hurry",
            setting = "A taxi in city traffic, late morning",
            goals = listOf(
                "Tell the driver your destination",
                "Respond to the driver's small talk",
                "Ask how long it will take",
                "Pay and ask for a receipt"
            ),
            items = listOf(
                ScenarioItem("arrived_receipt", "Receipt at Destination", "发票", "🧾")
            ),
            minLevel = ProficiencyLevel.BEGINNER,
            stampTitle = "City Navigator"
        ),
        Scenario(
            id = "bar_banter",
            title = "Bar Banter",
            hanziTitle = "酒吧聊天",
            emoji = "🍸",
            description = "Strike up a conversation with a stranger at a bar",
            aiRole = "a friendly local at the next bar stool",
            userRole = "a newcomer in town",
            setting = "A trendy cocktail bar on a Friday night",
            goals = listOf(
                "Open the conversation naturally",
                "Talk about where you're from and what you do",
                "Find something you both enjoy",
                "Exchange WeChat contacts"
            ),
            items = listOf(
                ScenarioItem("new_friend_wechat", "New Friend's WeChat", "微信好友", "💬")
            ),
            minLevel = ProficiencyLevel.INTERMEDIATE,
            stampTitle = "Bar Banter Fluent"
        ),
        Scenario(
            id = "office_smalltalk",
            title = "Office Small Talk",
            hanziTitle = "办公室闲聊",
            emoji = "💼",
            description = "Break the ice with a new colleague before a meeting",
            aiRole = "a new colleague waiting for the same meeting",
            userRole = "an employee at an international company",
            setting = "The office kitchen, ten minutes before a meeting",
            goals = listOf(
                "Introduce yourself and your team",
                "Chat about the week and workload",
                "Mention a project you're working on",
                "Agree to grab lunch together"
            ),
            items = listOf(
                ScenarioItem("lunch_invite", "Lunch Invitation", "午饭之约", "🥢")
            ),
            minLevel = ProficiencyLevel.INTERMEDIATE,
            stampTitle = "Boardroom Survivor"
        )
    )

    fun byId(id: String): Scenario? = scenarios.find { it.id == id }
}
