package com.udea.websocketsair;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/gates")
@CrossOrigin(origins = "http://localhost:5173")
public class GateController {
    private final SimpMessagingTemplate messagingTemplate;

    public GateController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    //Recibir informacion desde el cliente y enviar a todos los subscriptores
    @MessageMapping("/updateGate")
    @SendTo("/topic/gates")
    public GateInfo updateGateInfo(GateInfo gateInfo) throws Exception{
        System.out.println("Actualizandlo informacion de la puerta: "+ gateInfo.getGate());
        messagingTemplate.convertAndSend("/topic/gates", gateInfo);
        //Devolver al inforamcion de la puerta a todos lo subscriptores
        return new GateInfo(
                HtmlUtils.htmlEscape(gateInfo.getGate()),
                HtmlUtils.htmlEscape(gateInfo.getFlightNumber()),
                HtmlUtils.htmlEscape(gateInfo.getDestination()),
                HtmlUtils.htmlEscape(gateInfo.getDepartureTime()),
                HtmlUtils.htmlEscape(gateInfo.getStatus())
        );
    }

    //enviar actualizaciones programaticas o desde un servicio externo
    public void sendUpdate(GateInfo gateInfo){
        //enviar los datos actualizados de una puerta de enbarque a todos los subscriptores en /topic/gates
        messagingTemplate.convertAndSend("/topic/gates", gateInfo);
    }

    private Map<String,GateInfo> getData = new ConcurrentHashMap<>();


    //Metodos para actualizar la informacion de la puerta de enbarque
    @PostMapping("/update")
    public ResponseEntity<String> updateGate(@RequestBody GateInfo gate){
        //actuliazar los datos de la puerta de embarque
        getData.put(gate.getGate(), gate);
        //Enviar al actualizacion a toods los subscriptiores
        messagingTemplate.convertAndSend("/topic/gates", gate);
        return ResponseEntity.ok("Gate updated");
    }

    @GetMapping("/{gateNumber}")
    public ResponseEntity<GateInfo> getGateInfo(@PathVariable String gateNumber){
        //Obtener la informacion de la puerta de embarque
        GateInfo gateInfo = getData.get(gateNumber);
        if(gateInfo == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gateInfo);
    }
}
