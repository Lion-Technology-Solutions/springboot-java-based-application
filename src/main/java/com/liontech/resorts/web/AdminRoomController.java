package com.liontech.resorts.web;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.liontech.resorts.domain.RoomStatus;
import com.liontech.resorts.domain.RoomType;
import com.liontech.resorts.dto.RoomForm;
import com.liontech.resorts.service.BusinessException;
import com.liontech.resorts.service.RoomService;

@Controller
public class AdminRoomController {

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/admin/rooms")
    public String adminRooms(Model model) {
        model.addAttribute("rooms", roomService.listAllRooms());
        return "admin-rooms";
    }

    @GetMapping("/admin/rooms/new")
    public String newRoom(Model model) {
        prepareRoomForm(model, new RoomForm());
        return "add-room";
    }

    @PostMapping("/admin/rooms")
    public String addRoom(
        @Valid @ModelAttribute RoomForm roomForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareRoomForm(model, roomForm);
            return "add-room";
        }

        try {
            roomService.addRoom(roomForm);
        } catch (BusinessException exception) {
            bindingResult.rejectValue("roomNumber", "duplicate", exception.getMessage());
            prepareRoomForm(model, roomForm);
            return "add-room";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Room added to the LionTech Resorts catalog.");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/admin/rooms/seed")
    public String seedRooms(RedirectAttributes redirectAttributes) {
        int created = roomService.seedFullRoomCatalog();
        redirectAttributes.addFlashAttribute("successMessage", created + " room(s) added from the full resort catalog.");
        return "redirect:/admin/rooms";
    }

    private void prepareRoomForm(Model model, RoomForm roomForm) {
        model.addAttribute("roomForm", roomForm);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("amenities", roomService.listAmenities());
    }
}
