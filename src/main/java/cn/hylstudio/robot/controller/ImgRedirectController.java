package cn.hylstudio.robot.controller;

import cn.hylstudio.robot.service.group.IRevokeMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/img")
public class ImgRedirectController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImgRedirectController.class);

    @Autowired
    private IRevokeMsgService revokeMsgService;

    @RequestMapping(value = "/{imgCode:.+}", method = RequestMethod.GET)
    public void getImg(@PathVariable String imgCode, HttpServletResponse response) throws IOException {
        String imageUrl = revokeMsgService.getImageUrl(imgCode);
        LOGGER.info("redirect [{}]=>[{}]", imgCode, imageUrl);
        response.sendRedirect(imageUrl);
    }

}
