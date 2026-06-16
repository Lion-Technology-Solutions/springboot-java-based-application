package com.liontech.resorts.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.liontech.resorts.service.FacilityService;
import com.liontech.resorts.service.RoomService;

@Controller
public class HomeController {

    private final RoomService roomService;
    private final FacilityService facilityService;

    public HomeController(RoomService roomService, FacilityService facilityService) {
        this.roomService = roomService;
        this.facilityService = facilityService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rooms", roomService.listAvailableRooms());
        model.addAttribute("facilities", facilityService.listFacilities());
        return "index";
    }

    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomService.listAvailableRooms());
        return "rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getRoom(id));
        return "room-detail";
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("facilities", facilityService.listFacilities());
        return "facilities";
    }
}
