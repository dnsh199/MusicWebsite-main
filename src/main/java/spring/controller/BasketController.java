package spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import spring.model.Basket;
import spring.model.Content;
import spring.model.PurchaseDetails;
import spring.model.Song;
import spring.service.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/basket")
public class BasketController {
    @Autowired
    SongService songService;
    @Autowired
    BasketService basketService;
    @Autowired
    ContentService contentService;
    @Autowired
    PurchaseDetailsService purchaseDetailsService;
    @Autowired
    UserService userService;

    @GetMapping("/basketContent/{songId}/{basketId}")
    public ModelAndView addToBasket(@PathVariable int songId, @PathVariable int basketId) throws SQLException {
        Content content = new Content();
        content.setSongId(songId);
        content.setBasket(basketService.getBasketById(basketId));
        contentService.insertContent(content);
        Basket basket = basketService.getBasketById(basketId);
        ModelAndView modelAndView = new ModelAndView("addedToBasket");
        modelAndView.addObject("userId",basket.getUserId());
        return modelAndView;
    }
    @GetMapping("/basketStatus/{userId}")
    public ModelAndView myBasket(@PathVariable int userId) throws SQLException {
        ModelAndView modelAndView = new ModelAndView();
        if((basketService.getBasketByUserId(userId,"Pending")==null)&&(basketService.getBasketByUserId(userId,"Approved")==null)){
            modelAndView.setViewName("nullBasket");
        }else if(basketService.getBasketByUserId(userId,"Pending")==null){
                if(basketService.getBasketByUserId(userId,"Approved")!=null){
                    Basket basket = basketService.getBasketByUserId(userId,"Approved");
                    basketHandling(userId, modelAndView, basket);
                    modelAndView.setViewName("approvedBasket");
                }
        }else if((basketService.getBasketByUserId(userId,"Pending")!=null)&&(basketService.getBasketByUserId(userId,"Approved")==null)){
            Basket basket = basketService.getBasketByUserId(userId,"Pending");
            basketHandling(userId,modelAndView,basket);
            modelAndView.setViewName("pendingBasket");
        }else if((basketService.getBasketByUserId(userId,"Pending")!=null)&&(basketService.getBasketByUserId(userId,"Approved")!=null)){
            Basket  pendingBasket = basketService.getBasketByUserId(userId,"Pending");
            Basket approvedBasket = basketService.getBasketByUserId(userId,"Approved");
            List<Content> pendingBasketContent=  pendingBasket.getContentList();
            List<Song> pendingBasketSongs=new ArrayList<>();
            List<Content> approvedBasketContent=approvedBasket.getContentList();
            List<Song> approvedBasketSongs=new ArrayList<>();
            for(Content content : pendingBasketContent){
                pendingBasketSongs.add(songService.getSongById(content.getSongId()));
            }
            modelAndView.addObject("pendingSongList",pendingBasketSongs);
            modelAndView.addObject("song",new Song());
            modelAndView.addObject("userId", userId);
            for(Content content : approvedBasketContent){
                approvedBasketSongs.add(songService.getSongById(content.getSongId()));
            }
            modelAndView.addObject("approvedSongList",approvedBasketSongs);
            modelAndView.setViewName("twoBaskets");
        }return modelAndView;
    }

    private void basketHandling(int userId, ModelAndView modelAndView, Basket basket) throws SQLException {
        List<Content> contentList = basket.getContentList();
        List<Song> songList = new ArrayList<>();
        for(Content content : contentList){
            songList.add(songService.getSongById(content.getSongId()));
        }
        modelAndView.addObject("songList",songList);
        modelAndView.addObject("song",new Song());
        modelAndView.addObject("userId", userId);
    }
    @GetMapping("/checkOut/{userId}")
    public ModelAndView checkOutProcess(@PathVariable int userId) throws SQLException {
        Basket basket = basketService.getBasketByUserId(userId,"Approved");
        List<Content> contentList = basket.getContentList();
        List<Song> songList = new ArrayList<>();
        long price = 0;
        for(Content content : contentList){
            songList.add(songService.getSongById(content.getSongId()));
        }
        for(Song song : songList){
            price = song.getPrice()+price;
        }
        PurchaseDetails purchaseDetails = new PurchaseDetails();
        purchaseDetails.setBillAmount(price);
        
    }
}
